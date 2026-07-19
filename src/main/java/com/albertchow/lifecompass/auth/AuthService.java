package com.albertchow.lifecompass.auth;

import com.albertchow.lifecompass.auth.dto.LoginResponse;
import com.albertchow.lifecompass.auth.dto.RegisterRequest;
import com.albertchow.lifecompass.common.enums.Role;
import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.albertchow.lifecompass.security.JwtUtil;
import com.albertchow.lifecompass.security.TwilioSmsSender;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String SMS_CODE_KEY_PREFIX = "login:code:";
    private static final Duration SMS_CODE_TTL = Duration.ofMinutes(5);

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final TwilioSmsSender smsSender;

    /** Requirement 1: Google login. Finds or creates a USER account. */
    public LoginResponse loginWithGoogle(String idTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(idTokenString);
        } catch (GeneralSecurityException | IllegalArgumentException | java.io.IOException e) {
            throw new BusinessException("Invalid Google ID token");
        }
        if (idToken == null) {
            throw new BusinessException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getGoogleId, googleId));
        if (user == null && email != null) {
            // Link an existing email-based account rather than creating a duplicate.
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        }
        if (user == null) {
            user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setNickName(name != null ? name : "User");
            user.setIcon(picture != null ? picture : "");
            user.setRole(Role.USER.name());
            user.setStatus(1);
            userMapper.insert(user);
        } else if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            userMapper.updateById(user);
        }
        return issueToken(user);
    }

    /** Requirement 1: step one of SMS login — send a 6-digit code via Twilio. */
    public void sendSmsCode(String phone) {
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 1_000_000));
        redisTemplate.opsForValue().set(SMS_CODE_KEY_PREFIX + phone, code, SMS_CODE_TTL);
        smsSender.send(phone, code);
    }

    /** Requirement 1: step two of SMS login — verify the code, find or create a USER account. */
    public LoginResponse loginWithSms(String phone, String code) {
        String key = SMS_CODE_KEY_PREFIX + phone;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(code)) {
            throw new BusinessException("Invalid or expired verification code");
        }
        redisTemplate.delete(key);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickName("User" + phone.substring(phone.length() - 4));
            user.setIcon("");
            user.setRole(Role.USER.name());
            user.setStatus(1);
            userMapper.insert(user);
        }
        return issueToken(user);
    }

    /** Email+password login, available to any self-registered account regardless of role. */
    public LoginResponse loginWithCredentials(String email, String rawPassword) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null || user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }
        return issueToken(user);
    }

    /** Self-registration with email+password. Only USER and MERCHANT are self-selectable; see {@link RegisterRequest}. */
    public LoginResponse register(RegisterRequest request) {
        boolean emailTaken = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, request.email())) != null;
        if (emailTaken) {
            throw new BusinessException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickName(request.nickName());
        user.setIcon("");
        user.setCity(request.city() != null ? request.city() : "");
        user.setRole(request.role());
        user.setStatus(1);
        userMapper.insert(user);
        return issueToken(user);
    }

    private LoginResponse issueToken(User user) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("This account has been suspended");
        }
        Role role = Role.valueOf(user.getRole());
        String token = jwtUtil.generate(user.getId(), role);
        return new LoginResponse(token, user.getId(), user.getNickName(), role.name());
    }
}
