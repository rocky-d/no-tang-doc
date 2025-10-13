package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.document.*;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import com.ntdoc.notangdoccore.event.UserOperationEvent;
import com.ntdoc.notangdoccore.service.DocumentService;
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
    //日志发布者
    private final ApplicationEventPublisher eventPublisher;

    private final FileStorageService digitalOceanSpacesService;

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

            // 成功后记录上传成功的日志
            String username = jwt.getClaimAsString("preferred_username");

            eventPublisher.publishEvent(
                    UserOperationEvent.success(
                            this,
                            ActorType.USER,
                            username,
                            OperationType.UPLOAD_DOCUMENT,
                            fileName
                    )
            );

            return ResponseEntity.ok(ApiResponse.success("文件上传成功", response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload request: {}", e.getMessage());
            // 发布上传失败日志
            String username = jwt.getClaimAsString("preferred_username");

            eventPublisher.publishEvent(
                    UserOperationEvent.fail(
                            this,
                            ActorType.USER,
                            username,
                            OperationType.UPLOAD_DOCUMENT,
                            fileName,
                            e.getMessage()
                    )
            );
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

            // 记录下载日志
            String username = jwt.getClaimAsString("preferred_username");
            String documentName = documentService.getDocumentById(documentId,kcUserId).getStoredFilename();

            eventPublisher.publishEvent(
                    UserOperationEvent.success(
                            this,
                            ActorType.USER,
                            username,
                            OperationType.DOWNLOAD_DOCUMENT,
                            documentName
                    )
            );

            return ResponseEntity.ok(ApiResponse.success("获取下载链接成功", response));

        } catch (SecurityException e) {
            log.warn("Access denied for document {}: {}", documentId, e.getMessage());

            // 记录下载失败日志
            String username = jwt.getClaimAsString("preferred_username");
            String kcUserId = jwt.getClaimAsString("sub");
            String documentName = documentService.getDocumentById(documentId,kcUserId).getStoredFilename();

            eventPublisher.publishEvent(
                    UserOperationEvent.fail(
                            this,
                            ActorType.USER,
                            username,
                            OperationType.DOWNLOAD_DOCUMENT,
                            documentName,
                            e.getMessage()
                    )
            );

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

        // 记录删除文档日志
        String username = jwt.getClaimAsString("preferred_username");
        String documentName =document.getStoredFilename();

        eventPublisher.publishEvent(
                UserOperationEvent.success(
                        this,
                        ActorType.USER,
                        username,
                        OperationType.DELETE_DOCUMENT,
                        documentName
                )
        );

        return ResponseEntity.ok(response);
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
            log.error("Failed to generate share link for document");
            return ResponseEntity.internalServerError().body(
                    DocumentShareResponse.failure("generate share link failed")
            );
        }
    }
}
