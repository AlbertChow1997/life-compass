package com.albertchow.lifecompass.auth;

import com.albertchow.lifecompass.auth.dto.AuthConfigResponse;
import com.albertchow.lifecompass.auth.dto.CredentialLoginRequest;
import com.albertchow.lifecompass.auth.dto.GoogleLoginRequest;
import com.albertchow.lifecompass.auth.dto.LoginResponse;
import com.albertchow.lifecompass.auth.dto.SmsCodeRequest;
import com.albertchow.lifecompass.auth.dto.SmsLoginRequest;
import com.albertchow.lifecompass.auth.dto.UserDTO;
import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.TwilioSmsSender;
import com.albertchow.lifecompass.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final TwilioSmsSender smsSender;

    /** Lets the login page show accurate copy without exposing any Twilio secrets. */
    @GetMapping("/config")
    public Result<AuthConfigResponse> config() {
        return Result.ok(new AuthConfigResponse(smsSender.isConfigured()));
    }

    /** Requirement 1: Google login. */
    @PostMapping("/google")
    public Result<LoginResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return Result.ok(authService.loginWithGoogle(request.idToken()));
    }

    /** Requirement 1: send a Twilio SMS verification code. */
    @PostMapping("/sms/code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request.phone());
        return Result.ok();
    }

    /** Requirement 1: complete SMS login with the verification code. */
    @PostMapping("/sms/login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return Result.ok(authService.loginWithSms(request.phone(), request.code()));
    }

    /** Requirement 7: merchant/admin credential login. */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody CredentialLoginRequest request) {
        return Result.ok(authService.loginWithCredentials(request.email(), request.password()));
    }

    /** The currently authenticated account, resolved from the JWT. */
    @GetMapping("/me")
    public Result<UserDTO> me() {
        LoginUser loginUser = UserContext.require();
        User user = userMapper.selectById(loginUser.id());
        return Result.ok(new UserDTO(user.getId(), user.getNickName(), user.getIcon(), user.getRole()));
    }
}
