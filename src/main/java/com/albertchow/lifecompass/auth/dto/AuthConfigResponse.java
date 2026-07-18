package com.albertchow.lifecompass.auth.dto;

/** Tells the frontend which login methods are actually live, without exposing any secrets. */
public record AuthConfigResponse(boolean smsConfigured) {
}
