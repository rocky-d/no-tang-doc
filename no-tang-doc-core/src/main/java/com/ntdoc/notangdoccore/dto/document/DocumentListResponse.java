package com.ntdoc.notangdoccore.dto.document;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.Tag;
import lombok.*;
import java.util.List;
import java.util.Set;
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
        private List<DocumentTagResponse> documents;
    }



    public static DocumentListResponse fromDocuments(List<Document> documents) {
        List<DocumentTagResponse> responses = documents.stream()
                .map(DocumentTagResponse::form)
                .collect(Collectors.toList());

        return DocumentListResponse.builder()
                .code(200)
                .message("获取文档列表成功")
                .data(Data.builder().documents(responses).build())
                .build();
    }

    public static DocumentListResponse error(String message) {
        return DocumentListResponse.builder()
                .code(500)
                .message(message)
                .data(Data.builder().build())
                .build();
    }
}
