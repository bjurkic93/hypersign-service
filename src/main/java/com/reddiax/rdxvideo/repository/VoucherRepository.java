package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.constant.VoucherStatusEnum;
import com.reddiax.rdxvideo.model.entity.VoucherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {

    /**
     * Find voucher by unique code.
     */
    Optional<VoucherEntity> findByCode(String code);

    /**
     * Check if voucher code exists.
     */
    boolean existsByCode(String code);

    /**
     * Find all vouchers for a user.
     */
    Page<VoucherEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find all active vouchers for a user.
     */
    List<VoucherEntity> findByUserIdAndStatusOrderByExpiresAtAsc(Long userId, VoucherStatusEnum status);

    /**
     * Find all vouchers for a user advertisement.
     */
    List<VoucherEntity> findByUserAdvertisementIdOrderByCreatedAtDesc(Long userAdvertisementId);

    /**
     * Find all vouchers for an organization.
     */
    Page<VoucherEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId, Pageable pageable);

    /**
     * Find vouchers that need to be expired.
     */
    @Query("SELECT v FROM VoucherEntity v WHERE v.status = 'ACTIVE' AND v.expiresAt < :now")
    List<VoucherEntity> findExpiredVouchers(@Param("now") LocalDateTime now);

    /**
     * Bulk expire vouchers.
     */
    @Modifying
    @Query("UPDATE VoucherEntity v SET v.status = 'EXPIRED' WHERE v.status = 'ACTIVE' AND v.expiresAt < :now")
    int expireVouchers(@Param("now") LocalDateTime now);

    /**
     * Count active vouchers for a user.
     */
    @Query("SELECT COUNT(v) FROM VoucherEntity v WHERE v.user.id = :userId AND v.status = 'ACTIVE' AND v.expiresAt > :now")
    Long countActiveVouchersByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find active voucher by code (for redemption).
     */
    @Query("SELECT v FROM VoucherEntity v WHERE v.code = :code AND v.status = 'ACTIVE' AND v.expiresAt > :now")
    Optional<VoucherEntity> findActiveVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);
}
