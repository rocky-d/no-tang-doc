package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.exception.DocumentException;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.DocumentService;
import com.ntdoc.notangdoccore.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * 文档业务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${digitalocean.spaces.bucket}")
    private String bucketName;

    @Value("${app.file.max-size:52428800}")
    private long maxFileSize;

    @Override
    @Transactional
    public Document uploadDocument(MultipartFile file, String kcUserId, String description) {
        // 验证文件
        validateFile(file);

        // 查找用户
        User user = findUserByKcUserId(kcUserId);

        // 上传文件到存储
        String s3Key = fileStorageService.uploadFile(file, kcUserId);

        // 计算文件哈希（如果存储服务支持）
        String fileHash = null;
        if (fileStorageService instanceof DigitalOceanSpacesService) {
            try {
                fileHash = ((DigitalOceanSpacesService) fileStorageService).calculateFileHash(file);
            } catch (Exception e) {
                log.warn("Failed to calculate file hash, continuing without it: {}", e.getMessage());
            }
        }

        // 创建文档实体
        Document document = Document.builder()
                .originalFilename(file.getOriginalFilename())
                .storedFilename(extractFilenameFromS3Key(s3Key))
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .fileHash(fileHash)
                .s3Bucket(bucketName)
                .s3Key(s3Key)
                .uploadedBy(user)
                .status(Document.DocumentStatus.ACTIVE)
                .description(description)
                .downloadCount(0)
                .build();

        Document savedDocument = documentRepository.save(document);

        log.info("Document uploaded successfully: id={}, filename={}, user={}",
                savedDocument.getId(), file.getOriginalFilename(), kcUserId);

        return savedDocument;
    }

    @Override
    public URL getDownloadUrl(Long documentId, String kcUserId) {
        Document document = getDocumentById(documentId, kcUserId);

        // 生成15分钟有效期的下载链接
        URL downloadUrl = fileStorageService.generateDownloadUrl(document.getS3Key(), Duration.ofMinutes(15));

        // 异步增加下载次数
        incrementDownloadCount(documentId);

        log.info("Generated download URL for document: id={}, user={}", documentId, kcUserId);
        return downloadUrl;
    }

    // 获取指定员工的文档列表，但不筛选状态
    @Override
    public List<Document> getUserDocuments(String kcUserId) {
        User user = findUserByKcUserId(kcUserId);
        return documentRepository.findByUploadedByOrderByCreatedAtDesc(user);
    }

    // 获取指定员工的文档列表，且筛选指定状态
    @Override
    public List<Document> getUserDocuments(String kcUserId, Document.DocumentStatus status) {
        User user = findUserByKcUserId(kcUserId);
        return documentRepository.findByUploadedByAndStatusOrderByCreatedAtDesc(user, status);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, String kcUserId) {
        Document document = getDocumentById(documentId, kcUserId);

        // 软删除：更新状态而不是物理删除
        document.setStatus(Document.DocumentStatus.DELETED);
        documentRepository.save(document);

        // 可选：异步删除存储中的实际文件
        try {
            fileStorageService.deleteFile(document.getS3Key());
            log.info("Document deleted from storage: key={}", document.getS3Key());
        } catch (Exception e) {
            log.error("Failed to delete file from storage: key={}, error={}", document.getS3Key(), e.getMessage());
        }

        log.info("Document deleted: id={}, user={}", documentId, kcUserId);
    }

    @Override
    public Document getDocumentById(Long documentId, String kcUserId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document not found: " + documentId));

        // 权限检查：只能访问自己的文档
        if (!document.getUploadedBy().getKcUserId().equals(kcUserId)) {
            throw new DocumentException("Access denied: document belongs to another user");
        }

        // 检查文档状态
        if (document.getStatus() == Document.DocumentStatus.DELETED) {
            throw new DocumentException("Document has been deleted");
        }

        return document;
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document != null) {
                document.setDownloadCount(document.getDownloadCount() + 1);
                documentRepository.save(document);
            }
        } catch (Exception e) {
            log.error("Failed to increment download count for document: {}", documentId, e);
        }
    }

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentException("File cannot be empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new DocumentException("File name cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new DocumentException("File size exceeds maximum limit: " + maxFileSize + " bytes");
        }

        // 可以添加更多验证逻辑，如文件类型检查
        log.debug("File validation passed: {}, size: {}", file.getOriginalFilename(), file.getSize());
    }

    /**
     * 根据 Keycloak 用户ID 查找用户
     */
    private User findUserByKcUserId(String kcUserId) {
        return userRepository.findByKcUserId(kcUserId)
                .orElseThrow(() -> new DocumentException("User not found: " + kcUserId));
    }

    /**
     * 从 S3 键中提取文件名
     */
    private String extractFilenameFromS3Key(String s3Key) {
        if (s3Key == null) return null;
        int lastSlash = s3Key.lastIndexOf('/');
        return lastSlash >= 0 ? s3Key.substring(lastSlash + 1) : s3Key;
    }
}
