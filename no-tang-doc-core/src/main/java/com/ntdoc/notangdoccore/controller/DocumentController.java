package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.document.DeleteDocumentResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentDownloadResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentListResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "文档管理", description = "文档上传、下载、删除等操作")
public class DocumentController {
    private final DocumentService documentService;

    //文档上传
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文档", description = "上传文档文件到系统")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "上传成功")
    })
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "自定义文件名（可选）")
            @RequestParam(value = "fileName", required = false) String fileName,

            @Parameter(description = "文档描述（可选）")
            @RequestParam(value = "description", required = false) String description,

            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received document upload request: file={}, fileName={}",
                    file.getOriginalFilename(), fileName);

            String kcUserId = jwt.getClaimAsString("sub");

            DocumentUploadResponse response = documentService.uploadDocument(file, fileName, description, kcUserId);

            log.info("Document uploaded successfully: documentId={}, userId={}",
                    response.getDocumentId(), kcUserId);

            return ResponseEntity.ok(ApiResponse.success("文件上传成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "请求参数错误: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "文件上传失败: " + e.getMessage()));
        }
    }

    //获取文档下载链接
    @GetMapping("/download/{documentId}")
    @Operation(summary = "获取文档下载链接", description = "获取指定文档的预签名下载链接")
    public ResponseEntity<ApiResponse<DocumentDownloadResponse>> getDownloadUrl(
            @Parameter(description = "文档ID", required = true)
            @PathVariable Long documentId,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            log.info("Received download request for document: {}", documentId);

            String kcUserId = jwt.getClaimAsString("sub");

            DocumentDownloadResponse response = documentService.getDocumentDownloadUrl(documentId, kcUserId);

            log.info("Download URL generated successfully for document: {}", documentId);

            return ResponseEntity.ok(ApiResponse.success("获取下载链接成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(403, "无权访问该文档: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get download URL for document: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取下载链接失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<DocumentListResponse> getUserDocuments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) Document.DocumentStatus status
    ) {
        String kcUserId = jwt.getClaimAsString("sub"); // 从JWT中获取Keycloak用户ID（还需确定是否时sub）

        List<Document> documents;
        // 根据是否提供状态参数，选择调用不同的方法
        if (status != null) {
            documents = documentService.getUserDocuments(kcUserId, status);
        } else {
            documents = documentService.getUserDocuments(kcUserId);
        }

        DocumentListResponse response = DocumentListResponse.fromDocuments(documents);
        return ResponseEntity.ok(response);
    }

    // 删除指定文档
    @DeleteMapping("/{documentId}")
    public ResponseEntity<DeleteDocumentResponse> deleteDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal Jwt jwt) {
        String kcUserId = jwt.getClaimAsString("sub");

        Document document = documentService.getDocumentById(documentId, kcUserId);

        documentService.deleteDocument(documentId, kcUserId);

        DeleteDocumentResponse response = DeleteDocumentResponse.builder()
                .code(200)
                .message("文档删除成功")
                .documentId(documentId)
                .fileName(document.getOriginalFilename()) //.fileName(jwt.getClaimAsString("filename"))
                .deletedAt(Instant.now())
                .permanent(false)
                .recoveryDeadline(Instant.now().plusSeconds(30 * 24 * 3600)) // 30天恢复期
                .build();

        return ResponseEntity.ok(response);
    }
}
