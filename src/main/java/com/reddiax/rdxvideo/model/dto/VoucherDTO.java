package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.VoucherStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long userAdvertisementId;
    private String advertisementName;
    private Long organizationId;
    private String organizationName;
    private String code;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String description;
    private VoucherStatusEnum status;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private String usedReference;
    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime createdAt;
    
    // Computed fields
    private Boolean isValid;
    private Long daysUntilExpiry;
}
