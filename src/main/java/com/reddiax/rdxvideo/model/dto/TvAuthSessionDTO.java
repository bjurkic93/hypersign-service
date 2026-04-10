package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.model.entity.TvAuthSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for TV auth session information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvAuthSessionDTO {
    private String sessionId;
    private String sessionCode;
    private TvAuthSessionStatus status;
    private String qrCodeUrl;
    private LocalDateTime expiresAt;
    private Long expiresInSeconds;
    
    // Only populated after approval
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private String deviceToken;
    private Long organizationId;
    private String organizationName;
}
