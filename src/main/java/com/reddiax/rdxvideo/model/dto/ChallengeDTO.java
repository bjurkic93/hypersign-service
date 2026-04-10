package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeDTO {
    private Long id;
    private String nonce;
    private String encryptedChallengeData;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Long userAdvertisementId;
}
