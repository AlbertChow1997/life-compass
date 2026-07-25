package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.user.dto.UserProfileResponse;
import com.albertchow.lifecompass.user.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public "browse people" directory, separate from {@link UserController}'s
 * always-authenticated /api/user/** personal-center routes — deliberately a
 * different path prefix (/api/users) so it doesn't need to loosen that
 * controller's blanket auth requirement.
 */
@Tag(name = "Users", description = "Public directory of user accounts to browse and follow")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDirectoryController {

    private final UserService userService;

    /** Lists active users other than the caller, optionally filtered by a nickname substring. */
    @Operation(summary = "Browse/search users")
    @GetMapping
    public Result<List<UserSummaryResponse>> list(@RequestParam(required = false) String name) {
        return Result.ok(userService.listDirectory(name));
    }

    /** Public profile for one user: basic info, bio, XP/level data, and follow counts. */
    @Operation(summary = "Get one user's public profile")
    @GetMapping("/{id}")
    public Result<UserProfileResponse> profile(@PathVariable Long id) {
        return Result.ok(userService.getPublicProfile(id));
    }
}
