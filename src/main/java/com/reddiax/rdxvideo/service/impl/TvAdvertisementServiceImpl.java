package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.TvImpressionRequestDTO;
import com.reddiax.rdxvideo.model.dto.TvImpressionResponseDTO;
import com.reddiax.rdxvideo.model.entity.AdvertisementEntity;
import com.reddiax.rdxvideo.model.entity.PricingTierEntity;
import com.reddiax.rdxvideo.repository.AdvertisementRepository;
import com.reddiax.rdxvideo.repository.PricingTierRepository;
import com.reddiax.rdxvideo.service.TvAdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Implementation of TV advertisement service.
 * 
 * Handles TV ad impressions tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TvAdvertisementServiceImpl implements TvAdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final PricingTierRepository pricingTierRepository;

    @Override
    @Transactional
    public TvImpressionResponseDTO recordImpression(TvImpressionRequestDTO request) {
        log.info("Recording TV impression for ad {} by organization {}, duration: {} seconds",
                request.getAdvertisementId(), request.getDisplayerOrganizationId(), request.getDurationSeconds());

        // 1. Get the advertisement
        AdvertisementEntity ad = advertisementRepository.findById(request.getAdvertisementId())
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Advertisement not found: " + request.getAdvertisementId(), "AD_NOT_FOUND"));

        // Validate it's a TV advertisement
        if (ad.getPlatform() != AdvertisementPlatformEnum.TV) {
            throw new RdXException(HttpStatus.BAD_REQUEST, 
                    "Advertisement is not a TV ad", "INVALID_PLATFORM");
        }

        // Validate ad is active and valid
        LocalDateTime now = LocalDateTime.now();
        if (!ad.getActive() || now.isBefore(ad.getValidFrom()) || now.isAfter(ad.getValidTo())) {
            throw new RdXException(HttpStatus.BAD_REQUEST, 
                    "Advertisement is not currently active or valid", "AD_NOT_ACTIVE");
        }

        Long ownerOrgId = ad.getOrganizationId();
        Long displayerOrgId = request.getDisplayerOrganizationId();
        boolean isOwnAd = ownerOrgId != null && ownerOrgId.equals(displayerOrgId);
        boolean isPublicAd = Boolean.TRUE.equals(ad.getIsPublic());

        // 2. Calculate charge amount based on pricing tier (per minute)
        BigDecimal chargeAmount = calculateChargeAmount(ad, request.getDurationSeconds());

        log.info("TV impression recorded for ad {} - charge amount: {}", ad.getId(), chargeAmount);

        // 3. Build response
        String message = buildResponseMessage(isOwnAd, isPublicAd, chargeAmount);

        return TvImpressionResponseDTO.builder()
                .advertisementId(ad.getId())
                .ownerOrganizationId(ownerOrgId)
                .displayerOrganizationId(displayerOrgId)
                .durationSeconds(request.getDurationSeconds())
                .amountCharged(chargeAmount)
                .amountRewarded(BigDecimal.ZERO)
                .isPublicAd(isPublicAd)
                .rewardGiven(false)
                .message(message)
                .build();
    }

    /**
     * Calculate charge amount based on pricing tier (price per minute).
     */
    private BigDecimal calculateChargeAmount(AdvertisementEntity ad, Long durationSeconds) {
        if (ad.getPricingTierId() == null) {
            log.warn("No pricing tier set for ad {}, using default 1 RDX per minute", ad.getId());
            return calculatePerMinute(BigDecimal.ONE, durationSeconds);
        }

        PricingTierEntity pricingTier = pricingTierRepository.findById(ad.getPricingTierId())
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Pricing tier not found: " + ad.getPricingTierId(), "PRICING_TIER_NOT_FOUND"));

        return calculatePerMinute(pricingTier.getPrice(), durationSeconds);
    }

    /**
     * Calculate amount based on per-minute rate.
     * Amount = rate * (seconds / 60)
     */
    private BigDecimal calculatePerMinute(BigDecimal ratePerMinute, Long durationSeconds) {
        BigDecimal minutes = BigDecimal.valueOf(durationSeconds)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return ratePerMinute.multiply(minutes).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Build a descriptive response message.
     */
    private String buildResponseMessage(boolean isOwnAd, boolean isPublicAd, BigDecimal chargeAmount) {
        if (isOwnAd) {
            return String.format("Own ad displayed. Charge: %s", chargeAmount.stripTrailingZeros().toPlainString());
        } else if (isPublicAd) {
            return String.format("Public ad displayed. Charge: %s", chargeAmount.stripTrailingZeros().toPlainString());
        } else {
            return String.format("Private ad displayed. Charge: %s", chargeAmount.stripTrailingZeros().toPlainString());
        }
    }
}
