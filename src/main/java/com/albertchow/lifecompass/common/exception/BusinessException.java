package com.albertchow.lifecompass.common.exception;

/**
 * A recoverable, user-facing failure (bad credentials, expired code, etc.).
 * Caught by {@link GlobalExceptionHandler} and returned as {@code Result.fail(message)}.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
