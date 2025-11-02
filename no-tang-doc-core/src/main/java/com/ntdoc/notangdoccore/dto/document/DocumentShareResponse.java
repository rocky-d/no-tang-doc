package com.ntdoc.notangdoccore.dto.document;

import lombok.Data;

@Data
public class DocumentShareResponse {
    private boolean success;
    private String url;
    private Long documentId;
    private String s3Key;
    private Integer expirationMinutes;
    private String message;

    public static DocumentShareResponse success(String url,Long documentId,String s3Key,int expirationMinutes){
        DocumentShareResponse response = new DocumentShareResponse();
        response.setSuccess(true);
        response.setUrl(url);
        response.setDocumentId(documentId);
        response.setS3Key(s3Key);
        response.setExpirationMinutes(expirationMinutes);
        response.setMessage("generate share url success");
        return response;
    }

    public static DocumentShareResponse failure(String message){
        DocumentShareResponse response = new DocumentShareResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
