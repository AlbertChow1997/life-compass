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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Requirement 6: browse and purchase shop vouchers. */
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherMapper voucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final ShopMapper shopMapper;

    public List<Voucher> listOnShelf(Long shopId) {
        var query = new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getStatus, VoucherStatus.ON_SHELF.code());
        if (shopId != null) {
            query.eq(Voucher::getShopId, shopId);
        }
        query.orderByDesc(Voucher::getCreateTime);
        return voucherMapper.selectList(query);
    }

    public Voucher getById(Long id) {
        Voucher voucher = voucherMapper.selectById(id);
        if (voucher == null) {
            throw new NotFoundException("Voucher not found");
        }
        return voucher;
    }

    @Transactional
    public VoucherOrder purchase(Long voucherId, Long userId) {
        Voucher voucher = getById(voucherId);
        if (voucher.getStatus() == null || voucher.getStatus() != VoucherStatus.ON_SHELF.code()) {
            throw new BusinessException("This voucher is not currently available");
        }
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getBeginTime() != null && now.isBefore(voucher.getBeginTime())) {
            throw new BusinessException("This voucher is not yet available");
        }
        if (voucher.getEndTime() != null && now.isAfter(voucher.getEndTime())) {
            throw new BusinessException("This voucher has expired");
        }

        if (voucher.getType() != null && voucher.getType() == 1) {
            // Atomic decrement guarded by stock > 0 so concurrent purchases can't oversell.
            int updated = voucherMapper.update(null, new UpdateWrapper<Voucher>()
                    .setSql("stock = stock - 1")
                    .eq("id", voucherId)
                    .gt("stock", 0));
            if (updated == 0) {
                throw new BusinessException("This voucher is sold out");
            }
        }

        VoucherOrder order = new VoucherOrder();
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setPayType(1);
        order.setStatus(OrderStatus.PAID.code());
        order.setPayTime(now);
        voucherOrderMapper.insert(order);

        shopMapper.update(null, new UpdateWrapper<Shop>()
                .setSql("sold = sold + 1")
                .eq("id", voucher.getShopId()));

        return order;
    }
}
