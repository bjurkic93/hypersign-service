package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.constant.VoucherStatusEnum;
import com.reddiax.rdxvideo.model.dto.VoucherDTO;
import com.reddiax.rdxvideo.model.entity.VoucherEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class VoucherMapper {

    public VoucherDTO toDTO(VoucherEntity entity) {
        if (entity == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        
        return VoucherDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .userName(entity.getUser() != null ? entity.getUser().getDisplayName() : null)
                .userAdvertisementId(entity.getUserAdvertisement() != null ? 
                        entity.getUserAdvertisement().getId() : null)
                .advertisementName(entity.getUserAdvertisement() != null && 
                        entity.getUserAdvertisement().getAdvertisement() != null ?
                        entity.getUserAdvertisement().getAdvertisement().getName() : null)
                .organizationId(entity.getOrganization() != null ? entity.getOrganization().getId() : null)
                .organizationName(entity.getOrganization() != null ? entity.getOrganization().getName() : null)
                .code(entity.getCode())
                .discountPercentage(entity.getDiscountPercentage())
                .discountAmount(entity.getDiscountAmount())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .usedAt(entity.getUsedAt())
                .usedReference(entity.getUsedReference())
                .minPurchaseAmount(entity.getMinPurchaseAmount())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .createdAt(entity.getCreatedAt())
                .isValid(entity.getStatus() == VoucherStatusEnum.ACTIVE && entity.getExpiresAt().isAfter(now))
                .daysUntilExpiry(entity.getExpiresAt().isAfter(now) ? 
                        ChronoUnit.DAYS.between(now, entity.getExpiresAt()) : 0L)
                .build();
    }
}
