package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.enums.OrderStatus;
import com.albertchow.lifecompass.common.enums.VoucherStatus;
import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.VoucherMapper;
import com.albertchow.lifecompass.mapper.VoucherOrderMapper;
import com.albertchow.lifecompass.voucher.dto.CreateVoucherRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implements merchant-side voucher management: creating vouchers and toggling
 * them on/off shelf. Every action first checks that the merchant actually
 * owns the shop the voucher belongs to, to stop merchants touching each
 * other's listings.
 */
@Service
@RequiredArgsConstructor
public class MerchantVoucherService {

    private final VoucherMapper voucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final ShopMapper shopMapper;

    /** Creates a new voucher on-shelf for a shop, after confirming the merchant owns that shop. */
    public Voucher create(Long merchantId, CreateVoucherRequest request) {
        Shop shop = requireOwnedShop(merchantId, request.shopId());

        Voucher voucher = new Voucher();
        voucher.setShopId(shop.getId());
        voucher.setTitle(request.title());
        voucher.setSubTitle(request.subTitle() != null ? request.subTitle() : "");
        voucher.setRules(request.rules() != null ? request.rules() : "");
        voucher.setPayValue(request.payValue());
        voucher.setActualValue(request.actualValue());
        voucher.setType(request.type() != null ? request.type() : 0);
        voucher.setStock(request.stock() != null ? request.stock() : 0);
        voucher.setStatus(VoucherStatus.ON_SHELF.code());
        voucher.setBeginTime(request.beginTime());
        voucher.setEndTime(request.endTime());
        voucherMapper.insert(voucher);
        return voucher;
    }

    /** Flips a voucher on/off shelf, after confirming the merchant owns the shop it belongs to. */
    public Voucher setShelf(Long merchantId, Long voucherId, boolean onShelf) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new NotFoundException("Voucher not found");
        }
        requireOwnedShop(merchantId, voucher.getShopId());

        int newStatus = onShelf ? VoucherStatus.ON_SHELF.code() : VoucherStatus.OFF_SHELF.code();
        Voucher patch = new Voucher();
        patch.setId(voucherId);
        patch.setStatus(newStatus);
        voucherMapper.updateById(patch);

        voucher.setStatus(newStatus);
        return voucher;
    }

    /** Lists vouchers across every shop the merchant owns, or just one owned shop if shopId is given (rejecting shops they don't own). */
    public List<Voucher> listMine(Long merchantId, Long shopId) {
        List<Long> ownedShopIds = shopMapper.selectList(
                        new LambdaQueryWrapper<Shop>().eq(Shop::getOwnerId, merchantId))
                .stream().map(Shop::getId).toList();
        if (ownedShopIds.isEmpty()) {
            return List.of();
        }

        var query = new LambdaQueryWrapper<Voucher>().in(Voucher::getShopId, ownedShopIds);
        if (shopId != null) {
            if (!ownedShopIds.contains(shopId)) {
                throw new BusinessException("You do not own this shop");
            }
            query.eq(Voucher::getShopId, shopId);
        }
        return voucherMapper.selectList(query.orderByDesc(Voucher::getCreateTime));
    }

    /**
     * Redeems a customer's voucher order by the code shown on their order (a QR + text pair,
     * see VoucherService.purchase and MyOrdersPage.tsx) — looked up by the code alone, the
     * customer never sees or needs to know the underlying order id. Confirms the current
     * merchant owns the shop the voucher belongs to before marking it used.
     */
    public VoucherOrder redeemByCode(Long merchantId, String verifyCode) {
        VoucherOrder order = voucherOrderMapper.selectOne(
                new LambdaQueryWrapper<VoucherOrder>().eq(VoucherOrder::getVerifyCode, verifyCode));
        if (order == null) {
            throw new NotFoundException("No voucher found for this code");
        }
        if (order.getStatus() != null && order.getStatus() == OrderStatus.USED.code()) {
            throw new BusinessException("This voucher has already been redeemed");
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.PAID.code()) {
            throw new BusinessException("This voucher isn't valid for redemption");
        }

        Voucher voucher = voucherMapper.selectById(order.getVoucherId());
        if (voucher == null) {
            throw new NotFoundException("Voucher not found");
        }
        Shop shop = requireOwnedShop(merchantId, voucher.getShopId());

        VoucherOrder patch = new VoucherOrder();
        patch.setId(order.getId());
        patch.setStatus(OrderStatus.USED.code());
        patch.setUseTime(LocalDateTime.now());
        voucherOrderMapper.updateById(patch);

        order.setStatus(OrderStatus.USED.code());
        order.setVoucherTitle(voucher.getTitle());
        order.setShopName(shop.getName());
        return order;
    }

    /** Fetches a shop and verifies the given merchant is its owner, throwing if the shop is missing or owned by someone else. */
    private Shop requireOwnedShop(Long merchantId, Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new NotFoundException("Shop not found");
        }
        if (!merchantId.equals(shop.getOwnerId())) {
            throw new BusinessException("You do not own this shop");
        }
        return shop;
    }
}
