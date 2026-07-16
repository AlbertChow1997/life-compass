package com.albertchow.lifecompass.auth.dto;

public record LoginResponse(String token, Long userId, String nickName, String role) {
}
