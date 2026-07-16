package com.albertchow.lifecompass.common;

import lombok.Data;

import java.util.List;

/**
 * Uniform API response envelope returned by every controller.
 *
 * @param <T> payload type
 */
@Data
public class Result<T> {

    /** true when the request succeeded. */
    private boolean success;

    /** human-readable error message, null on success. */
    private String errorMsg;

    /** response payload, null on error. */
    private T data;

    /** total row count, populated for paginated list responses. */
    private Long total;

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.success = true;
        return r;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> Result<List<T>> ok(List<T> data, Long total) {
        Result<List<T>> r = new Result<>();
        r.success = true;
        r.data = data;
        r.total = total;
        return r;
    }

    public static <T> Result<T> fail(String errorMsg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.errorMsg = errorMsg;
        return r;
    }
}
