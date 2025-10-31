package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.document.*;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import com.ntdoc.notangdoccore.event.UserOperationEvent;
import com.ntdoc.notangdoccore.service.DocumentService;
import com.ntdoc.notangdoccore.service.DocumentTagService;
import com.ntdoc.notangdoccore.service.UserSyncService;
import com.ntdoc.notangdoccore.service.FileStorageService;
import com.ntdoc.notangdoccore.service.impl.DigitalOceanSpacesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "文档管理", description = "文档上传、下载、删除等操作")
public class DocumentController {
    private final DocumentService documentService;
    private final UserSyncService userSyncService;

    private final FileStorageService digitalOceanSpacesService;
    private final DocumentTagService documentTagService;

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

            if (response != null) {
                log.info("Document uploaded successfully: documentId={}, userId={}",
                        response.getDocumentId(), kcUserId);
            }

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

    /**
     * 获取指定用户的所有文档，支持按状态过滤
     */
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

    /**
     * 删除指定文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<DeleteDocumentResponse> deleteDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal Jwt jwt) {
        try{
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
        }catch (Exception e) {
            log.error("Failed to delete document: documentId={}",
                    documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DeleteDocumentResponse.builder()
                            .code(500)
                            .message("删除文档失败: " + e.getMessage())
                            .documentId(documentId)
                            .build());
        }

//        return ResponseEntity.noContent().build(); // HTTP 204
    }

    // 生成分享链接
    @GetMapping("/share")
    public ResponseEntity<DocumentShareResponse> generatePreviewShareLink(
            @RequestParam Long documentId,
            @RequestParam(defaultValue = "10") int expirationMinutes,
            @AuthenticationPrincipal Jwt jwt
    ){
        if (expirationMinutes < 1 ) {
            return ResponseEntity.badRequest().build();
        }
        try{
            // 先验证用户权限
            String kcUserId = jwt.getClaimAsString("sub");
            Document document = documentService.getDocumentById(documentId, kcUserId);

            if (document == null) {
                log.error("Document with id {} not found", documentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        DocumentShareResponse.failure("Document not fount in storage")
                );
            }


            String s3Key = document.getS3Key();

            if (s3Key == null || s3Key.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            //检查文件在文件存储中是否存在
            if (!digitalOceanSpacesService.fileExists(s3Key)) {
                log.warn("Document not fount in storage");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        DocumentShareResponse.failure("Document not fount in storage")
                );
            }

            // 生成默认10分钟的有效预览链接
            URL shareURL = digitalOceanSpacesService.generateShareUrl(s3Key, Duration.ofMinutes(expirationMinutes));

            DocumentShareResponse response = DocumentShareResponse.success(
                    shareURL.toString(),
                    document.getId(),
                    s3Key,
                    expirationMinutes
            );

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            log.error("Failed to generate share link for document:{}",e.getMessage());
            return ResponseEntity.internalServerError().body(
                    DocumentShareResponse.failure("generate share link failed")
            );
        }
    }

    // Tag Function
    /**
     * Upload Document with Tags
     */
    @PostMapping("/upload-with-tags")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocumentsWithTags(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value="fileName",required = false) String fileName,
            @RequestParam(value="description",required = false) String description,
            @RequestParam(value="tags",required = false) List<String> tags,
            @AuthenticationPrincipal Jwt jwt
    ){
        try {
            String kcUserId = jwt.getClaimAsString("sub");
            DocumentUploadResponse response =
                    documentService.uploadDocument(file, fileName, description, kcUserId);

            if (tags != null && !tags.isEmpty()) {
                documentTagService.addTags(response.getDocumentId(), tags, kcUserId);
            }

            return ResponseEntity.ok(ApiResponse.success("文件上传并添加标签成功", response));
        } catch (Exception e) {
            log.error("Failed to upload document with tags", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "文件上传或标签添加失败: " + e.getMessage()));
        }
    }


    /**
     * Add Tags For ExistDocument
     */
    @PostMapping("/{documentId}/tags")
    @Operation(summary = "为文档添加标签")
    public ResponseEntity<ApiResponse<Document>> addTagsToDocument(
            @PathVariable Long documentId,
            @RequestBody List<String> tags,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            String kcUserId = jwt.getClaimAsString("sub");
            Document updated = documentTagService.addTags(documentId, tags, kcUserId);
            return ResponseEntity.ok(ApiResponse.success("标签添加成功", updated));
        } catch (Exception e) {
            log.error("Failed to add tags for document {}", documentId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "标签添加失败: " + e.getMessage()));
        }
    }

    /**
     * Get All Tags in Document
     */
    @GetMapping("/{documentId}/tags")
    @Operation(summary = "获取文档的所有标签")
    public ResponseEntity<ApiResponse<List<String>>> getTagsByDocument(
            @PathVariable Long documentId
    ) {
        List<String> tagNames = documentTagService.getTags(documentId)
                .stream()
                .map(com.ntdoc.notangdoccore.entity.Tag::getTag)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("查询成功", tagNames));
    }

    /**
     * Delete one tag in document
     */
    @DeleteMapping("/{documentId}/tags/{tagName}")
    @Operation(summary = "删除文档标签")
    public ResponseEntity<ApiResponse<Document>> removeTagFromDocument(
            @PathVariable Long documentId,
            @PathVariable String tagName,
            @AuthenticationPrincipal Jwt jwt
    ) {
        try {
            String kcUserId = jwt.getClaimAsString("sub");
            Document updated = documentTagService.removeTag(documentId, tagName, kcUserId);
            return ResponseEntity.ok(ApiResponse.success("标签删除成功", updated));
        } catch (Exception e) {
            log.error("Failed to remove tag {} from document {}", tagName, documentId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "标签删除失败: " + e.getMessage()));
        }
    }

    /**
     * find document by Tag
     */
    @GetMapping("/by-tag/{tagName}")
    @Operation(summary = "根据标签名获取文档列表")
    public ResponseEntity<DocumentListResponse> getDocumentsByTag(@PathVariable String tagName) {
        try {
            List<Document> docs = documentTagService.getDocumentsByTag(tagName);
            DocumentListResponse response = DocumentListResponse.fromDocuments(docs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get documents by tag '{}': {}", tagName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DocumentListResponse.error("Get documents by tag fail: " + e.getMessage()));
        }
    }
    /**
     * 根据文件名搜索文档
     */
    @GetMapping("/search")
    public ResponseEntity<DocumentListResponse> searchByFilename(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("keyword") String keyword
    ) {
        String kcUserId = jwt.getClaimAsString("sub");
        List<Document> docs = documentService.searchDocumentsByFilename(kcUserId, keyword);
        DocumentListResponse response = DocumentListResponse.fromDocuments(docs);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据所有者、文件类型、上传日期进行过滤
     */
    @GetMapping("/filter")
    public ResponseEntity<DocumentListResponse> filterDocuments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "contentType", required = false) String contentType,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr
    ) {
        String kcUserId = jwt.getClaimAsString("sub");

        Instant start = startDateStr != null ? Instant.parse(startDateStr) : null;
        Instant end = endDateStr != null ? Instant.parse(endDateStr) : null;

        List<Document> docs = documentService.filterDocuments(kcUserId, contentType, start, end);

        DocumentListResponse response = DocumentListResponse.fromDocuments(docs);
        return ResponseEntity.ok(response);
    }

}
