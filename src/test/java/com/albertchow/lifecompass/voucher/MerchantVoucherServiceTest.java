package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.enums.OrderStatus;
import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.VoucherMapper;
import com.albertchow.lifecompass.mapper.VoucherOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link MerchantVoucherService#redeemByCode}, the newest
 * feature covered by this report — every rejection path (unknown code,
 * already redeemed, not yet paid, wrong merchant) plus the happy path,
 * mirroring the live functional test reported in the technical report's
 * Section 2.7.2 but runnable without a live backend.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MerchantVoucherServiceTest {

    private static final Long MERCHANT_ID = 1L;
    private static final Long OTHER_MERCHANT_ID = 2L;
    private static final Long SHOP_ID = 10L;
    private static final String CODE = "123456";

    @Mock
    private VoucherMapper voucherMapper;
    @Mock
    private VoucherOrderMapper voucherOrderMapper;
    @Mock
    private ShopMapper shopMapper;

    private MerchantVoucherService service;

    @BeforeEach
    void setUp() {
        service = new MerchantVoucherService(voucherMapper, voucherOrderMapper, shopMapper);
    }

    private VoucherOrder paidOrder() {
        VoucherOrder order = new VoucherOrder();
        order.setId(100L);
        order.setVoucherId(5L);
        order.setStatus(OrderStatus.PAID.code());
        order.setVerifyCode(CODE);
        return order;
    }

    private Voucher voucher() {
        Voucher v = new Voucher();
        v.setId(5L);
        v.setShopId(SHOP_ID);
        v.setTitle("EUR 20 Dinner Voucher");
        return v;
    }

    private Shop ownedShop(Long ownerId) {
        Shop s = new Shop();
        s.setId(SHOP_ID);
        s.setOwnerId(ownerId);
        s.setName("Test Shop");
        return s;
    }

    @Test
    void redeem_succeeds_marksUsed_andEnrichesTitleAndShopName() {
        when(voucherOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(paidOrder());
        when(voucherMapper.selectById(5L)).thenReturn(voucher());
        when(shopMapper.selectById(SHOP_ID)).thenReturn(ownedShop(MERCHANT_ID));

        VoucherOrder result = service.redeemByCode(MERCHANT_ID, CODE);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.USED.code());
        assertThat(result.getVoucherTitle()).isEqualTo("EUR 20 Dinner Voucher");
        assertThat(result.getShopName()).isEqualTo("Test Shop");

        ArgumentCaptor<VoucherOrder> captor = ArgumentCaptor.forClass(VoucherOrder.class);
        verify(voucherOrderMapper).updateById(captor.capture());
        VoucherOrder patch = captor.getValue();
        assertThat(patch.getId()).isEqualTo(100L);
        assertThat(patch.getStatus()).isEqualTo(OrderStatus.USED.code());
        assertThat(patch.getUseTime()).isNotNull();
    }

    @Test
    void redeem_throwsNotFound_whenCodeDoesNotMatchAnyOrder() {
        when(voucherOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.redeemByCode(MERCHANT_ID, "000000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No voucher found for this code");
        verify(voucherOrderMapper, never()).updateById(any(VoucherOrder.class));
    }

    @Test
    void redeem_throwsAlreadyRedeemed_whenOrderIsAlreadyUsed() {
        VoucherOrder used = paidOrder();
        used.setStatus(OrderStatus.USED.code());
        when(voucherOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(used);

        assertThatThrownBy(() -> service.redeemByCode(MERCHANT_ID, CODE))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This voucher has already been redeemed");
        verify(voucherOrderMapper, never()).updateById(any(VoucherOrder.class));
    }

    @Test
    void redeem_throwsNotValid_whenOrderIsNotYetPaid() {
        VoucherOrder unpaid = paidOrder();
        unpaid.setStatus(OrderStatus.UNPAID.code());
        when(voucherOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(unpaid);

        assertThatThrownBy(() -> service.redeemByCode(MERCHANT_ID, CODE))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This voucher isn't valid for redemption");
    }

    @Test
    void redeem_throwsOwnershipError_andNeverMutatesTheOrder_whenTheMerchantDoesNotOwnTheShop() {
        when(voucherOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(paidOrder());
        when(voucherMapper.selectById(5L)).thenReturn(voucher());
        when(shopMapper.selectById(SHOP_ID)).thenReturn(ownedShop(OTHER_MERCHANT_ID));

        assertThatThrownBy(() -> service.redeemByCode(MERCHANT_ID, CODE))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You do not own this shop");

        // The most important assertion in this class: an ownership failure must not
        // leave the order half-redeemed.
        verify(voucherOrderMapper, never()).updateById(any(VoucherOrder.class));
    }
}
