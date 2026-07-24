package com.albertchow.lifecompass.common.exception;

import com.albertchow.lifecompass.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Catches exceptions thrown by any controller and turns them into a
 * {@link Result#fail(String)} response instead of a raw stack trace, so the
 * frontend always gets the same JSON error shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Maps a recoverable business failure to HTTP 400 with its message. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ex.getMessage()));
    }

    /** Maps a missing-resource failure to HTTP 404 with its message. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail(ex.getMessage()));
    }

    /** Maps @Valid request body failures to HTTP 400, joining all field errors into one message. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(message));
    }
}
