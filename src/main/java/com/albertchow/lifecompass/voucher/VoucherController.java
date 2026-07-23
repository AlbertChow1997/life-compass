package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public endpoints for browsing available vouchers and purchasing them. */
@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    /** Lists vouchers currently on shelf (purchasable), optionally narrowed to one shop. */
    @GetMapping
    public Result<List<Voucher>> list(@RequestParam(required = false) Long shopId) {
        return Result.ok(voucherService.listOnShelf(shopId));
    }

    /** Fetches full details for a single voucher. */
    @GetMapping("/{id}")
    public Result<Voucher> detail(@PathVariable Long id) {
        return Result.ok(voucherService.getById(id));
    }

    /** Purchases a voucher on behalf of the logged-in user. */
    @PostMapping("/{id}/purchase")
    public Result<VoucherOrder> purchase(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        return Result.ok(voucherService.purchase(id, userId));
    }
}
