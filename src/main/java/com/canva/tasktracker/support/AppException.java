package com.canva.tasktracker.support;

public class AppException extends RuntimeException {
    private final AppErrorCode code;

    public AppException(AppErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public AppException(AppErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public AppErrorCode code() { return code; }
}
