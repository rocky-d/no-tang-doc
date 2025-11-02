package com.ntdoc.notangdoccore.dto.document;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档上传响应DTO
 */
@Data
@Builder
public class DocumentUploadResponse {
    private Long documentId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String s3Key;
    private LocalDateTime uploadTime;
    private String userId;
    private String url;
    private String description;
    private List<String> tags;

}
