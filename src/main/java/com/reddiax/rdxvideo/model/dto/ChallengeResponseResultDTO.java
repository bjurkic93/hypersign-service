package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeResponseResultDTO {
    private Long challengeId;
    private Boolean signatureValid;
    private Boolean wallpaperHashMatch;
    private Boolean attestationValid;
    private Boolean overallValid;
    private Integer newTrustScore;
    private String message;
}
