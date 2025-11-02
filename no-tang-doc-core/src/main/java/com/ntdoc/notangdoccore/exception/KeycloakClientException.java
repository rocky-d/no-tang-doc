package com.ntdoc.notangdoccore.exception;

import lombok.Getter;

@Getter
public class KeycloakClientException extends RuntimeException {
    private final String error;
    private final String errorDescription;
    private final int status;
    private final Object payload;

    public KeycloakClientException(String error, String errorDescription, int status, Object payload) {
        super(error + ": " + errorDescription + " (status=" + status + ")");
        this.error = error;
        this.errorDescription = errorDescription;
        this.status = status;
        this.payload = payload;
    }

}