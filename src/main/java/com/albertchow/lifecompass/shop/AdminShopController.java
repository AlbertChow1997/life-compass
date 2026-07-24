package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.shop.dto.ShopUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints for creating and editing shop listings. All paths are
 * under /api/admin/**, which Spring Security restricts to ROLE_ADMIN
 * (see SecurityConfig).
 */
@Tag(name = "Admin - Shops", description = "Admin-only shop listing management (requires ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    /** Creates a new shop listing. */
    @Operation(summary = "Create a shop listing")
    @PostMapping
    public Result<Shop> create(@Valid @RequestBody ShopUpsertRequest request) {
        return Result.ok(shopService.create(request));
    }

    /** Updates an existing shop listing's details. */
    @Operation(summary = "Update a shop listing")
    @PutMapping("/{id}")
    public Result<Shop> update(@PathVariable Long id, @Valid @RequestBody ShopUpsertRequest request) {
        return Result.ok(shopService.update(id, request));
    }
}
