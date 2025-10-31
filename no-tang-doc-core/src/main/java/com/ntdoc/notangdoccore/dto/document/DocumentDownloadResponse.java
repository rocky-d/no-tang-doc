package com.ntdoc.notangdoccore.dto.document;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 文档下载响应DTO
 */
@Data
@Builder
public class DocumentDownloadResponse {
    private Long documentId;
    private String fileName;
    private String downloadUrl;
    private Instant expiresAt;
    private Long fileSize;
    private String mimeType;
}
