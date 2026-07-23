package com.albertchow.lifecompass.support;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.SupportFaq;
import com.albertchow.lifecompass.entity.SupportMessage;
import com.albertchow.lifecompass.support.dto.SupportFaqRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only endpoints for managing the support FAQ keyword list and viewing
 * the log of questions visitors have asked. All paths are under
 * /api/admin/**, which Spring Security restricts to ROLE_ADMIN (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportService supportService;

    /** Lists every FAQ entry, newest first. */
    @GetMapping("/faq")
    public Result<List<SupportFaq>> listFaq() {
        return Result.ok(supportService.listFaq());
    }

    /** Adds a new FAQ entry (keywords + answer) that the support widget can match questions against. */
    @PostMapping("/faq")
    public Result<SupportFaq> createFaq(@Valid @RequestBody SupportFaqRequest request) {
        return Result.ok(supportService.createFaq(request));
    }

    /** Updates an existing FAQ entry's keywords and answer. */
    @PutMapping("/faq/{id}")
    public Result<SupportFaq> updateFaq(@PathVariable Long id, @Valid @RequestBody SupportFaqRequest request) {
        return Result.ok(supportService.updateFaq(id, request));
    }

    /** Removes an FAQ entry entirely. */
    @DeleteMapping("/faq/{id}")
    public Result<Void> deleteFaq(@PathVariable Long id) {
        supportService.deleteFaq(id);
        return Result.ok();
    }

    /** Lists every question visitors have submitted through the support widget, newest first. */
    @GetMapping("/messages")
    public Result<List<SupportMessage>> listMessages() {
        return Result.ok(supportService.listMessages());
    }
}
