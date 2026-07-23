package com.albertchow.lifecompass.support;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.support.dto.AskSupportRequest;
import com.albertchow.lifecompass.support.dto.SupportAnswerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public support widget: works whether or not the visitor is signed in. */
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    /** Answers a visitor's question by matching it against the FAQ keyword list, falling back to a generic reply if nothing matches. */
    @PostMapping("/ask")
    public Result<SupportAnswerResponse> ask(@Valid @RequestBody AskSupportRequest request) {
        LoginUser loginUser = UserContext.get();
        Long userId = loginUser != null ? loginUser.id() : null;
        return Result.ok(supportService.ask(request.question(), userId));
    }
}
