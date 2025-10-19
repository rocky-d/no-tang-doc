package com.ntdoc.notangdoccore.exception;

/**
 * 团队相关异常
 */
public class TeamException extends RuntimeException {

    public TeamException(String message) {
        super(message);
    }

    public TeamException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 团队未找到异常
     */
    public static class TeamNotFoundException extends TeamException {
        public TeamNotFoundException(Long teamId) {
            super("Team not found: " + teamId);
        }
    }

    /**
     * 团队访问权限异常
     */
    public static class TeamAccessDeniedException extends TeamException {
        public TeamAccessDeniedException(String message) {
            super("Access denied: " + message);
        }
    }

    /**
     * 团队名称重复异常
     */
    public static class TeamNameDuplicateException extends TeamException {
        public TeamNameDuplicateException(String name) {
            super("Team name already exists: " + name);
        }
    }
}

