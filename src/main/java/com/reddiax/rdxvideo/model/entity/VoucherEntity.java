package com.reddiax.rdxvideo.model.entity;

import com.reddiax.rdxvideo.constant.VoucherStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for discount vouchers given as rewards.
 * Created when user earns a DISCOUNT type reward.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "VOUCHER", indexes = {
    @Index(name = "idx_voucher_code", columnList = "code", unique = true),
    @Index(name = "idx_voucher_user", columnList = "user_id"),
    @Index(name = "idx_voucher_status", columnList = "status")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class VoucherEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_advertisement_id")
    private UserAdvertisementEntity userAdvertisement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    // Unique voucher code (e.g., "RDX-ABCD-1234-EFGH")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    // Discount percentage (e.g., 10.00 for 10%)
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    // Optional: Fixed discount amount instead of percentage
    @Column(name = "discount_amount", precision = 19, scale = 4)
    private BigDecimal discountAmount;

    // Description of the voucher
    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VoucherStatusEnum status = VoucherStatusEnum.ACTIVE;

    // When the voucher expires
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // When the voucher was used
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // Reference to what was purchased with the voucher
    @Column(name = "used_reference", length = 255)
    private String usedReference;

    // Minimum purchase amount required to use voucher
    @Column(name = "min_purchase_amount", precision = 19, scale = 4)
    private BigDecimal minPurchaseAmount;

    // Maximum discount amount (cap for percentage discounts)
    @Column(name = "max_discount_amount", precision = 19, scale = 4)
    private BigDecimal maxDiscountAmount;

    // Helper methods
    public boolean isValid() {
        return status == VoucherStatusEnum.ACTIVE && LocalDateTime.now().isBefore(expiresAt);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markAsUsed(String reference) {
        this.status = VoucherStatusEnum.USED;
        this.usedAt = LocalDateTime.now();
        this.usedReference = reference;
    }

    public void expire() {
        this.status = VoucherStatusEnum.EXPIRED;
    }

    public void cancel() {
        this.status = VoucherStatusEnum.CANCELLED;
    }
}
