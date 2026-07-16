package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.enums.VoucherStatus;
import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.VoucherMapper;
import com.albertchow.lifecompass.voucher.dto.CreateVoucherRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Requirement 7: merchants create vouchers for their own shop and toggle them on/off shelf. */
@Service
@RequiredArgsConstructor
public class MerchantVoucherService {

    private final VoucherMapper voucherMapper;
    private final ShopMapper shopMapper;

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
