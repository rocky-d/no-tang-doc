package com.ntdoc.notangdoccore.exception;

/**
 * 文档相关异常
 */
public class DocumentException extends RuntimeException {

    public DocumentException(String message) {
        super(message);
    }

    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 文件未找到异常
     */
    public static class DocumentNotFoundException extends DocumentException {
        public DocumentNotFoundException(Long documentId) {
            super("Document not found: " + documentId);
        }
    }

    /**
     * 文件访问权限异常
     */
    public static class DocumentAccessDeniedException extends DocumentException {
        public DocumentAccessDeniedException(String message) {
            super("Access denied: " + message);
        }
    }

    /**
     * 文件上传异常
     */
    public static class FileUploadException extends DocumentException {
        public FileUploadException(String message) {
            super("File upload failed: " + message);
        }

        public FileUploadException(String message, Throwable cause) {
            super("File upload failed: " + message, cause);
        }
    }
}