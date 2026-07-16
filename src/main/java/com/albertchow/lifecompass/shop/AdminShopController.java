package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.shop.dto.ShopUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin management of shop listings (secured by /api/admin/** -> ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @PostMapping
    public Result<Shop> create(@Valid @RequestBody ShopUpsertRequest request) {
        return Result.ok(shopService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Shop> update(@PathVariable Long id, @Valid @RequestBody ShopUpsertRequest request) {
        return Result.ok(shopService.update(id, request));
    }
}
