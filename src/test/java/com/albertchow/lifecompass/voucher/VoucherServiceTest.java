package com.albertchow.lifecompass.voucher;

import com.albertchow.lifecompass.common.enums.VoucherStatus;
import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.VoucherMapper;
import com.albertchow.lifecompass.mapper.VoucherOrderMapper;
import com.albertchow.lifecompass.shop.ShopCacheService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VoucherService#purchase}, covering every rejection
 * path (off-shelf, not-yet-started, expired, sold out) and the atomic
 * stock-decrement described in the technical report's Section 2.2.3 — the
 * mapper layer is mocked, so these run without a database and without real
 * concurrency, but they do verify that a sold-out voucher (the atomic
 * UPDATE affecting zero rows) is rejected *before* any order is inserted.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class VoucherServiceTest {

    @Mock
    private VoucherMapper voucherMapper;
    @Mock
    private VoucherOrderMapper voucherOrderMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private ShopCacheService shopCacheService;

    private VoucherService voucherService;

    @BeforeEach
    void setUp() {
        voucherService = new VoucherService(voucherMapper, voucherOrderMapper, shopMapper, shopCacheService);
    }

    private Voucher onShelfVoucher(int type) {
        Voucher v = new Voucher();
        v.setId(1L);
        v.setShopId(10L);
        v.setTitle("Test Voucher");
        v.setType(type);
        v.setStatus(VoucherStatus.ON_SHELF.code());
        v.setBeginTime(null);
        v.setEndTime(null);
        return v;
    }

    @Test
    void purchase_succeedsAndGeneratesASixDigitVerifyCode_forARegularVoucher() {
        when(voucherMapper.selectById(1L)).thenReturn(onShelfVoucher(0));

        VoucherOrder result = voucherService.purchase(1L, 42L);

        ArgumentCaptor<VoucherOrder> captor = ArgumentCaptor.forClass(VoucherOrder.class);
        verify(voucherOrderMapper).insert(captor.capture());
        VoucherOrder inserted = captor.getValue();

        assertThat(inserted.getUserId()).isEqualTo(42L);
        assertThat(inserted.getVoucherId()).isEqualTo(1L);
        assertThat(inserted.getStatus()).isEqualTo(2); // OrderStatus.PAID
        assertThat(inserted.getVerifyCode()).matches("\\d{6}");
        assertThat(result).isSameAs(inserted);

        // Type 0 (unlimited) must never touch the stock-decrement path.
        verify(voucherMapper, never()).update(any(), any(UpdateWrapper.class));
        verify(shopCacheService).evict(10L);
    }

    @Test
    void purchase_throwsNotFound_whenVoucherDoesNotExist() {
        when(voucherMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> voucherService.purchase(99L, 42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Voucher not found");
        verify(voucherOrderMapper, never()).insert(any(VoucherOrder.class));
    }

    @Test
    void purchase_throwsBusinessException_whenVoucherIsOffShelf() {
        Voucher v = onShelfVoucher(0);
        v.setStatus(VoucherStatus.OFF_SHELF.code());
        when(voucherMapper.selectById(1L)).thenReturn(v);

        assertThatThrownBy(() -> voucherService.purchase(1L, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This voucher is not currently available");
        verify(voucherOrderMapper, never()).insert(any(VoucherOrder.class));
    }

    @Test
    void purchase_throwsBusinessException_whenNotYetStarted() {
        Voucher v = onShelfVoucher(0);
        v.setBeginTime(LocalDateTime.now().plusDays(1));
        when(voucherMapper.selectById(1L)).thenReturn(v);

        assertThatThrownBy(() -> voucherService.purchase(1L, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This voucher is not yet available");
    }

    @Test
    void purchase_throwsBusinessException_whenExpired() {
        Voucher v = onShelfVoucher(0);
        v.setEndTime(LocalDateTime.now().minusDays(1));
        when(voucherMapper.selectById(1L)).thenReturn(v);

        assertThatThrownBy(() -> voucherService.purchase(1L, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This voucher has expired");
    }

    @Test
    void purchase_succeeds_forALimitedVoucher_whenTheAtomicDecrementAffectsARow() {
        when(voucherMapper.selectById(1L)).thenReturn(onShelfVoucher(1));
        when(voucherMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);

        VoucherOrder result = voucherService.purchase(1L, 42L);

        assertThat(result.getVerifyCode()).matches("\\d{6}");
        verify(voucherOrderMapper).insert(any(VoucherOrder.class));
    }

    @Test
    void purchase_throwsSoldOut_andNeverInsertsAnOrder_whenTheAtomicDecrementAffectsNoRows() {
        // updated == 0 is exactly the "someone else bought the last unit first" case the
        // atomic SQL guard (Section 2.2.3) is designed to make impossible to race past.
        when(voucherMapper.selectById(1L)).thenReturn(onShelfVoucher(1));
        when(voucherMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(0);

        assertThatThrownBy(() -> voucherService.purchase(1L, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This voucher is sold out");

        verify(voucherOrderMapper, never()).insert(any(VoucherOrder.class));
        verify(shopMapper, never()).update(any(), any());
    }
}
