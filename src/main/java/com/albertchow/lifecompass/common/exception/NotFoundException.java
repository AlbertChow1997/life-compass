package com.albertchow.lifecompass.common.exception;

/** A referenced resource (shop, blog post, voucher, ...) does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
