package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.PricingTierDTO;
import com.reddiax.rdxvideo.model.dto.PricingTierRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PricingTierService {

    Page<PricingTierDTO> getPricingTiers(Pageable pageable);

    List<PricingTierDTO> getActivePricingTiers();

    PricingTierDTO getPricingTier(Long id);

    PricingTierDTO createPricingTier(PricingTierRequestDTO request);

    PricingTierDTO updatePricingTier(Long id, PricingTierRequestDTO request);

    void deletePricingTier(Long id);
}
