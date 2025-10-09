package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.document.DocumentDownloadResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.DocumentService;
import com.ntdoc.notangdoccore.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
                    .originalFilename(originalFilename)
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

            return DocumentDownloadResponse.builder()
                    .documentId(document.getId())
                    .fileName(document.getOriginalFilename())
                    .downloadUrl(downloadUrl.toString())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .fileSize(document.getFileSize())
                    .mimeType(document.getContentType())
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate download URL for document: {}", documentId, e);
            throw new RuntimeException("生成下载链接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDocument(Long documentId, String kcUserId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getUserDocuments(String kcUserId) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public List<Document> getUserDocuments(String kcUserId, Document.DocumentStatus status) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentById(Long documentId, String kcUserId) {
        throw new UnsupportedOperationException("Method not implemented yet");
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
}
