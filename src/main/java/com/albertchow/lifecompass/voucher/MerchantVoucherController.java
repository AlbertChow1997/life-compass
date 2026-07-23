package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.voucher.dto.CreateVoucherRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lets merchants manage vouchers for the shops they own: listing their own
 * vouchers, creating new ones, and taking them on/off shelf. All paths are
 * under /api/merchant/**, which Spring Security restricts to ROLE_MERCHANT
 * (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/merchant/voucher")
@RequiredArgsConstructor
public class MerchantVoucherController {

    private final MerchantVoucherService merchantVoucherService;

    /** Lists the current merchant's own vouchers, optionally narrowed to one shop they own. */
    @GetMapping
    public Result<List<Voucher>> mine(@RequestParam(required = false) Long shopId) {
        Long merchantId = UserContext.require().id();
        return Result.ok(merchantVoucherService.listMine(merchantId, shopId));
    }

    /** Creates a new voucher for a shop the current merchant owns. */
    @PostMapping
    public Result<Voucher> create(@Valid @RequestBody CreateVoucherRequest request) {
        Long merchantId = UserContext.require().id();
        return Result.ok(merchantVoucherService.create(merchantId, request));
    }

    /** Puts a voucher on or off shelf (i.e. makes it purchasable or not), if the current merchant owns its shop. */
    @PutMapping("/{id}/shelf")
    public Result<Voucher> setShelf(@PathVariable Long id, @RequestParam boolean onShelf) {
        Long merchantId = UserContext.require().id();
        return Result.ok(merchantVoucherService.setShelf(merchantId, id, onShelf));
    }
}
