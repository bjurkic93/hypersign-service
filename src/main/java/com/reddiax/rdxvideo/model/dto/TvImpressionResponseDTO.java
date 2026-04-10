package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for a TV advertisement impression record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvImpressionResponseDTO {

    /**
     * The advertisement ID
     */
    private Long advertisementId;

    /**
     * Organization that owns the advertisement
     */
    private Long ownerOrganizationId;

    /**
     * Organization that displayed the advertisement
     */
    private Long displayerOrganizationId;

    /**
     * Duration in seconds
     */
    private Long durationSeconds;

    /**
     * Amount charged to the ad owner
     */
    private BigDecimal amountCharged;

    /**
     * Amount rewarded to the displayer (0 if own ad or not public)
     */
    private BigDecimal amountRewarded;

    /**
     * Whether the ad was public (eligible for publisher reward)
     */
    private Boolean isPublicAd;

    /**
     * Whether reward was given (true if public ad from different org)
     */
    private Boolean rewardGiven;

    /**
     * Message describing the transaction
     */
    private String message;
}
