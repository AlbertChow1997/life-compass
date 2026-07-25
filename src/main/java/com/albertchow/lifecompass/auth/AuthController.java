package com.albertchow.lifecompass.auth;

import com.albertchow.lifecompass.auth.dto.AuthConfigResponse;
import com.albertchow.lifecompass.auth.dto.CredentialLoginRequest;
import com.albertchow.lifecompass.auth.dto.GoogleLoginRequest;
import com.albertchow.lifecompass.auth.dto.LoginResponse;
import com.albertchow.lifecompass.auth.dto.RegisterRequest;
import com.albertchow.lifecompass.auth.dto.SmsCodeRequest;
import com.albertchow.lifecompass.auth.dto.SmsLoginRequest;
import com.albertchow.lifecompass.auth.dto.UserDTO;
import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.TwilioSmsSender;
import com.albertchow.lifecompass.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes all the ways a visitor can sign in or register: Google sign-in,
 * SMS one-time-code login, and classic email+password login/registration.
 * Delegates the actual business logic to {@link AuthService} and just wires
 * up the HTTP endpoints.
 */
@Tag(name = "Auth", description = "Login, registration, and the current user's own profile")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final TwilioSmsSender smsSender;

    /** Tells the frontend whether SMS login is available, so it can hide that option instead of exposing Twilio secrets. */
    @Operation(summary = "Whether SMS login is live (Twilio configured) vs. dev-mode logging")
    @GetMapping("/config")
    public Result<AuthConfigResponse> config() {
        return Result.ok(new AuthConfigResponse(smsSender.isConfigured()));
    }

    /** Signs a user in (or registers them) using a Google ID token from the frontend's Google sign-in widget. */
    @Operation(summary = "Log in with a Google ID token")
    @PostMapping("/google")
    public Result<LoginResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return Result.ok(authService.loginWithGoogle(request.idToken()));
    }

    /** Sends a one-time SMS verification code to the given phone number, as the first step of SMS login. */
    @Operation(summary = "Send a one-time SMS verification code")
    @PostMapping("/sms/code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request.phone());
        return Result.ok();
    }

    /** Finishes SMS login by checking the code the user typed in against the one that was texted to them. */
    @Operation(summary = "Log in with a phone number and SMS code")
    @PostMapping("/sms/login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return Result.ok(authService.loginWithSms(request.phone(), request.code()));
    }

    /** Email+password login (self-registered users, merchants, and admins). */
    @Operation(summary = "Log in with email and password")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody CredentialLoginRequest request) {
        return Result.ok(authService.loginWithCredentials(request.email(), request.password()));
    }

    /** Self-registration with email+password. */
    @Operation(summary = "Self-register with email and password (USER or MERCHANT role)")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    /** Returns basic profile details for whichever account the request's JWT belongs to. */
    @Operation(summary = "Get the current user's own profile")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public Result<UserDTO> me() {
        LoginUser loginUser = UserContext.require();
        User user = userMapper.selectById(loginUser.id());
        return Result.ok(new UserDTO(user.getId(), user.getNickName(), user.getIcon(), user.getCity(), user.getBio(), user.getRole()));
    }
}
