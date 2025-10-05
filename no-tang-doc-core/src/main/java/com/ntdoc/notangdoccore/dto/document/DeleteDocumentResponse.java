package com.ntdoc.notangdoccore.dto.document;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDocumentResponse {
    private int code;
    private String message;
    private Long documentId;
    private String fileName;
    private Instant deletedAt;
    private boolean permanent;
    private Instant recoveryDeadline;
}
