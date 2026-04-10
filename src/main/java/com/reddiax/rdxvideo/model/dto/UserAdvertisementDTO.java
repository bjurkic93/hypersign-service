package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.UserAdvertisementStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdvertisementDTO {
    private Long id;
    private Long userId;
    private Long advertisementId;
    private String advertisementName;
    private String advertisementImageUrl;
    private UserAdvertisementStatusEnum status;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime deactivatedAt;
    private Integer verificationCount;
    private LocalDateTime lastVerifiedAt;
    private Long totalValidSeconds;
    private String deviceInfo;
    private String deactivationReason;
    
    private Long remainingSeconds;
    private Long elapsedSeconds;
    private Boolean isExpired;
}
