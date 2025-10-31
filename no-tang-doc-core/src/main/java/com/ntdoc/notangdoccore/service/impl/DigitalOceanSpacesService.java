package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * DigitalOcean Spaces 文件存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalOceanSpacesService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${digitalocean.spaces.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String kcUserId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String s3Key = generateStoragePath(kcUserId, file.getOriginalFilename());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

            PutObjectResponse response = s3Client.putObject(putRequest, requestBody);

            log.info("File uploaded successfully: key={}, etag={}, size={}", s3Key, response.eTag(), file.getSize());
            return s3Key;

        } catch (IOException e) {
            log.error("Failed to read file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to read file content", e);
        } catch (Exception e) {
            log.error("Failed to upload file: key={}", s3Key, e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }

    @Override
    public URL generateDownloadUrl(String s3Key, Duration expiration) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getRequest)
                    .build();

            URL url = s3Presigner.presignGetObject(presignRequest).url();
            log.debug("Generated download URL for key: {}", s3Key);
            return url;

        } catch (Exception e) {
            log.error("Failed to generate download URL for key: {}", s3Key, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    @Override
    public URL generateShareUrl(String s3Key,Duration expiration){
        try{
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .responseContentDisposition("inline")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getRequest)
                    .build();

            URL url = s3Presigner.presignGetObject(presignRequest).url();
            log.debug("Generated share URL for key: {}", s3Key);
            return url;
        }catch (Exception e){
            log.error("Failed to generate share URL for key: {}", s3Key, e);
            throw new RuntimeException("Failed to generate share URL", e);
        }
    }

    @Override
    public URL generateUploadUrl(String s3Key, String contentType, Duration expiration) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .putObjectRequest(putRequest)
                    .build();

            URL url = s3Presigner.presignPutObject(presignRequest).url();
            log.debug("Generated upload URL for key: {}", s3Key);
            return url;

        } catch (Exception e) {
            log.error("Failed to generate upload URL for key: {}", s3Key, e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    @Override
    public boolean deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted successfully: key={}", s3Key);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete file: key={}", s3Key, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check file existence: key={}", s3Key, e);
            return false;
        }
    }

    @Override
    public String generateStoragePath(String kcUserId, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));

        // 生成唯一标识
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        // 清理文件名
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String finalFilename = uniqueId + "-" + sanitizedFilename;

        // 生成路径: documents/{kcUserId}/{year}/{month}/{uniqueId}-{filename}
        String path = String.format("documents/%s/%s/%s/%s", kcUserId, year, month, finalFilename);

        log.debug("Generated storage path: {} for user: {}", path, kcUserId);
        return path;
    }

    /**
     * 计算文件MD5哈希值
     */
    public String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = file.getBytes();
            byte[] hash = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Failed to calculate file hash: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }

    /**
     * 清理文件名，移除不安全字符
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed-file";
        }

        // 移除不安全字符
        String sanitized = filename.trim()
                .replaceAll("[/\\\\:*?\"<>|\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (sanitized.isEmpty()) {
            sanitized = "file";
        }

        // 限制长度
        if (sanitized.length() > 100) {
            String extension = "";
            int lastDot = sanitized.lastIndexOf(".");
            if (lastDot > 0) {
                extension = sanitized.substring(lastDot);
                sanitized = sanitized.substring(0, Math.min(100 - extension.length(), lastDot)) + extension;
            } else {
                sanitized = sanitized.substring(0, 100);
            }
        }

        return sanitized;
    }
}
