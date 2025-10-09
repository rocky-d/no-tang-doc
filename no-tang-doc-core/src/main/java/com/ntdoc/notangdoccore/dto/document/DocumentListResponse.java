package com.ntdoc.notangdoccore.dto.document;

import com.ntdoc.notangdoccore.entity.Document;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentListResponse {
    private int code;
    private String message;
    private Data data;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private List<DocumentItem> documents;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentItem {
        private String documentId;
        private String fileName;
        private long fileSize;
        private String mimeType;
        private String description;
        private String uploadTime;
        private String lastModified;
    }

    public static DocumentListResponse fromDocuments(List<Document> documents) {
        List<DocumentItem> items = documents.stream()
                .map(d -> DocumentItem.builder()
                        .documentId(String.valueOf(d.getId()))
                        .fileName(d.getOriginalFilename())
                        .fileSize(d.getFileSize())
                        .mimeType(d.getContentType())
                        //.tags()
                        .description(d.getDescription())
                        .uploadTime(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null)
                        .lastModified(d.getUpdatedAt() != null ? d.getUpdatedAt().toString() : null)
                        //.thumbnailUrl()
                        //.canPreview()
                        //.shared()
                        //.metadata()
                        .build())
                .collect(Collectors.toList());

        return DocumentListResponse.builder()
                .code(200)
                .message("获取文档列表成功")
                .data(Data.builder().documents(items).build())
                .build();
    }
}
