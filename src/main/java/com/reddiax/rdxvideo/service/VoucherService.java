package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.VoucherDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing discount vouchers.
 */
public interface VoucherService {

    /**
     * Create a voucher for a user as a reward.
     * 
     * @param userId User who receives the voucher
     * @param userAdvertisementId The advertisement that earned this voucher
     * @param discountPercentage Discount percentage (e.g., 10.00 for 10%)
     * @param description Description of the voucher
     * @param validDays Number of days the voucher is valid
     * @return Created voucher DTO
     */
    VoucherDTO createRewardVoucher(Long userId, Long userAdvertisementId, BigDecimal discountPercentage, 
                                   String description, int validDays);

    /**
     * Create a voucher for a user with organization branding.
     */
    VoucherDTO createOrganizationVoucher(Long userId, Long userAdvertisementId, Long organizationId,
                                         BigDecimal discountPercentage, String description, int validDays);

    /**
     * Get voucher by ID.
     */
    VoucherDTO getVoucherById(Long voucherId);

    /**
     * Get voucher by code.
     */
    VoucherDTO getVoucherByCode(String code);

    /**
     * Get all vouchers for current user.
     */
    Page<VoucherDTO> getMyVouchers(Pageable pageable);

    /**
     * Get active vouchers for current user.
     */
    List<VoucherDTO> getMyActiveVouchers();

    /**
     * Get vouchers for a specific user (admin).
     */
    Page<VoucherDTO> getVouchersByUserId(Long userId, Pageable pageable);

    /**
     * Redeem a voucher.
     * 
     * @param code Voucher code
     * @param reference Reference to the purchase/redemption
     * @return Redeemed voucher DTO
     */
    VoucherDTO redeemVoucher(String code, String reference);

    /**
     * Validate a voucher code without redeeming.
     */
    VoucherDTO validateVoucher(String code);

    /**
     * Cancel a voucher (admin).
     */
    void cancelVoucher(Long voucherId);

    /**
     * Expire all overdue vouchers.
     * Called by scheduler.
     * 
     * @return Number of expired vouchers
     */
    int expireOverdueVouchers();

    /**
     * Count active vouchers for current user.
     */
    Long countMyActiveVouchers();
}
