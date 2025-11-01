package com.ntdoc.notangdoccore.dto.document;

import com.ntdoc.notangdoccore.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTagResponse {
    private Long documentId;
    private String fileName;
    private String status;
    private Long fileSize;
    private String mimeType;
    private String description;
    private String uploadTime;
    private String lastModified;
    private List<String> tags;

    public static DocumentTagResponse form(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentTagResponse.builder()
                .documentId(document.getId())
                .fileName(document.getOriginalFilename())
                .mimeType(document.getContentType())
                .status(document.getStatus() != null ? document.getStatus().name() : null)
                .tags(document.getTags() != null ?
                        document.getTags().stream()
                                .map(tag -> tag.getTag())
                                .collect(Collectors.toList()) :
                        List.of())
                .fileSize(document.getFileSize())
                .description(document.getDescription())
                .uploadTime(document.getCreatedAt().toString())
                .lastModified(document.getUpdatedAt().toString())
                .build();
    }
}
