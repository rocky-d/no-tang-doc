package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.document.DocumentDownloadResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import com.ntdoc.notangdoccore.event.UserOperationEvent;
import com.ntdoc.notangdoccore.exception.DocumentException;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.DocumentSpecification;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.DocumentService;
import com.ntdoc.notangdoccore.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    //日志发布放在服务中而不是Controller里
    private final ApplicationEventPublisher eventPublisher;

    @Value("${digitalocean.spaces.bucket}")
    private String bucketName;

    @Value("${digitalocean.spaces.public-url}")
    private String publicUrl;

    @Override
    public DocumentUploadResponse uploadDocument(MultipartFile file, String fileName, String description, String kcUserId) {
        log.info("Starting document upload for user: {}, file: {}", kcUserId, file.getOriginalFilename());

        validateFile(file);
        User user = getUserByKcUserId(kcUserId);
        String originalFilename = file.getOriginalFilename();
        String finalFileName = StringUtils.hasText(fileName) ? fileName : originalFilename;

        try {
            String s3Key = fileStorageService.uploadFile(file, kcUserId);
            log.info("File uploaded to S3 successfully: key={}", s3Key);

            String fileHash = calculateFileHash(file);

            Document document = Document.builder()
                    .originalFilename(finalFileName)
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

            document = documentRepository.save(document);
            log.info("Document saved to database: id={}", document.getId());

            // 发布文件上传成功日志
            eventPublisher.publishEvent(
                    UserOperationEvent.success(
                            this,
                            ActorType.USER,
                            user.getUsername(),
                            user.getId(),
                            document.getId(),
                            OperationType.UPLOAD_DOCUMENT,
                            finalFileName
                    )
            );
            log.info("Publish Upload Document Successful Log");

            return DocumentUploadResponse.builder()
                    .documentId(document.getId())
                    .fileName(finalFileName)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .s3Key(s3Key)
                    .uploadTime(document.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .userId(kcUserId)
                    .url(generatePublicUrl(s3Key))
                    .description(description)
                    .build();

        } catch (Exception e) {
            // 发布上传失败日志
            eventPublisher.publishEvent(
                    UserOperationEvent.fail(
                            this,
                            ActorType.USER,
                            user.getUsername(),
                            user.getId(),
                            OperationType.UPLOAD_DOCUMENT,
                            fileName,
                            e.getMessage()
                    )
            );

            log.error("Failed to upload document: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownloadResponse getDocumentDownloadUrl(Long documentId, String kcUserId) {
        log.info("Getting download URL for document: {} by user: {}", documentId, kcUserId);

        // 内联实现文档获取和权限验证
        User user = getUserByKcUserId(kcUserId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + documentId));

        if (!document.getUploadedBy().getId().equals(user.getId())) {
            throw new SecurityException("无权访问该文档");
        }

        if (document.getStatus() == Document.DocumentStatus.DELETED) {
            throw new RuntimeException("文档已被删除: " + documentId);
        }

        try {
            URL downloadUrl = fileStorageService.generateDownloadUrl(document.getS3Key(), Duration.ofMinutes(60));

            incrementDownloadCount(documentId);

            // 发布下载文档日志
            eventPublisher.publishEvent(
                    UserOperationEvent.success(
                            this,
                            ActorType.USER,
                            user.getUsername(),
                            user.getId(),
                            documentId,
                            OperationType.DOWNLOAD_DOCUMENT,
                            document.getOriginalFilename()
                    )
            );
            log.info("Record Download Document Successful Log");

            return DocumentDownloadResponse.builder()
                    .documentId(document.getId())
                    .fileName(document.getOriginalFilename())
                    .downloadUrl(downloadUrl.toString())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .fileSize(document.getFileSize())
                    .mimeType(document.getContentType())
                    .build();

        } catch (Exception e) {
            eventPublisher.publishEvent(
                    UserOperationEvent.fail(
                            this,
                            ActorType.USER,
                            user.getUsername(),
                            user.getId(),
                            OperationType.DOWNLOAD_DOCUMENT,
                            document.getOriginalFilename(),
                            e.getMessage()
                    )
            );
            log.error("Failed to generate download URL for document: {}", documentId, e);
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDocument(Long documentId, String kcUserId) {
        log.info("Starting document delete for document: {} by user: {}", documentId, kcUserId);

        User user = getUserByKcUserId(kcUserId);
        Document document = getDocumentById(documentId, kcUserId);

        // 物理删除
        try{
            documentRepository.delete(document);
            fileStorageService.deleteFile(document.getS3Key());

            // 发布用户删除文档日志
            eventPublisher.publishEvent(
                    UserOperationEvent.success(
                            this,
                            ActorType.USER,
                            user.getUsername(),
                            user.getId(),
                            documentId,
                            OperationType.DELETE_DOCUMENT,
                            document.getOriginalFilename()
                    )
            );
            log.info("Record Delete Document Successful Log");

        } catch (Exception e) {
            // 发布删除失败日志
            eventPublisher.publishEvent(
                    UserOperationEvent.fail(
                            this,
                            ActorType.USER,
                            user.getUsername(),
                            user.getId(),
                            OperationType.DELETE_DOCUMENT,
                            document.getOriginalFilename(),
                            e.getMessage()
                    )
            );
            log.error("Failed to delete document: {},{}", documentId, e.getMessage());
        }
        // 软删除：只是更新状态，可恢复
//        document.setStatus(Document.DocumentStatus.DELETED);
//        documentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getUserDocuments(String kcUserId) {
        User user = getUserByKcUserId(kcUserId);
        return documentRepository.findByUploadedByOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Document> getUserDocuments(String kcUserId, Document.DocumentStatus status) {
        User user = getUserByKcUserId(kcUserId);
        return documentRepository.findByUploadedByAndStatusOrderByCreatedAtDesc(user, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentById(Long documentId, String kcUserId) {
        User user = getUserByKcUserId(kcUserId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + documentId));

//        if (!document.getUploadedBy().getId().equals(user.getId())) {
//            throw new SecurityException("无权删除该文档");
//        }
        if (!document.getUploadedBy().equals(user)) {
            throw new SecurityException("You are not allowed to delete this document.");
        }

        return document;
    }

    @Override
    public void incrementDownloadCount(Long documentId) {
        documentRepository.findById(documentId).ifPresent(document -> {
            document.setDownloadCount(document.getDownloadCount() + 1);
            documentRepository.save(document);
        });
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }

        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || filename.length() > 255) {
            throw new IllegalArgumentException("文件名无效或过长");
        }
    }

    private User getUserByKcUserId(String kcUserId) {
        return userRepository.findByKcUserId(kcUserId)
                .orElseGet(() -> {
                    log.info("User not found, creating new user with kcUserId: {}", kcUserId);
                    User newUser = User.builder()
                            .kcUserId(kcUserId)
                            .username("user_" + kcUserId.substring(0, 8))
                            .email("user@example.com")
                            .build();
                    return userRepository.save(newUser);
                });
    }

    private String extractFilenameFromS3Key(String s3Key) {
        return s3Key.substring(s3Key.lastIndexOf('/') + 1);
    }

    private String generatePublicUrl(String s3Key) {
        return publicUrl + "/" + s3Key;
    }

    private String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("Failed to calculate file hash, using timestamp instead", e);
            return String.valueOf(System.currentTimeMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> searchDocumentsByFilename(String kcUserId, String nameOrKeyword) {
        log.info("Searching document by name for keyword: {} by user: {}", nameOrKeyword, kcUserId);

        if (nameOrKeyword == null || nameOrKeyword.isBlank()) {
            throw new DocumentException("Search keyword must not be empty");
        }

        User user = getUserByKcUserId(kcUserId);
        return documentRepository.findByUploadedByAndOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(user, nameOrKeyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> filterDocuments(String kcUserId, String contentType, Instant start, Instant end) {
        User user = getUserByKcUserId(kcUserId);

        // 初始 Specification 直接用 uploadedBy 条件
        Specification<Document> spec = DocumentSpecification.uploadedBy(user);

        // 根据文件类型过滤
        if (contentType != null && !contentType.isBlank()) {
            spec = spec.and(DocumentSpecification.fileTypeEquals(contentType));
        }

        // 根据上传日期过滤
        if (start != null) {
            spec = spec.and(DocumentSpecification.uploadedAfter(start));
        }

        if (end != null) {
            spec = spec.and(DocumentSpecification.uploadedBefore(end));
        }

        return documentRepository.findAll(spec);
    }

}
