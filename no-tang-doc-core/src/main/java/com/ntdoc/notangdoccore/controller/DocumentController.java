package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.dto.document.DeleteDocumentResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentListResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    // 获取当前用户的所有文档
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
