package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public endpoints for browsing available vouchers and purchasing them. */
@Tag(name = "Vouchers", description = "Browse and purchase vouchers")
@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    /** Lists vouchers currently on shelf (purchasable), optionally narrowed to one shop. */
    @Operation(summary = "List on-shelf vouchers")
    @GetMapping
    public Result<List<Voucher>> list(@RequestParam(required = false) Long shopId) {
        return Result.ok(voucherService.listOnShelf(shopId));
    }

    /** Fetches full details for a single voucher. */
    @Operation(summary = "Get one voucher's detail")
    @GetMapping("/{id}")
    public Result<Voucher> detail(@PathVariable Long id) {
        return Result.ok(voucherService.getById(id));
    }

    /** Purchases a voucher on behalf of the logged-in user. */
    @Operation(summary = "Purchase a voucher")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/purchase")
    public Result<VoucherOrder> purchase(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        return Result.ok(voucherService.purchase(id, userId));
    }
}
