package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.PricingTierDTO;
import com.reddiax.rdxvideo.model.dto.PricingTierRequestDTO;
import com.reddiax.rdxvideo.model.entity.PricingTierEntity;
import com.reddiax.rdxvideo.model.mapper.PricingTierMapper;
import com.reddiax.rdxvideo.repository.PricingTierRepository;
import com.reddiax.rdxvideo.service.PricingTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingTierServiceImpl implements PricingTierService {

    private final PricingTierRepository pricingTierRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PricingTierDTO> getPricingTiers(Pageable pageable) {
        return pricingTierRepository.findAll(pageable)
                .map(PricingTierMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingTierDTO> getActivePricingTiers() {
        return pricingTierRepository.findByActiveTrue()
                .stream()
                .map(PricingTierMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PricingTierDTO getPricingTier(Long id) {
        return PricingTierMapper.INSTANCE.toDto(findPricingTierById(id));
    }

    @Override
    @Transactional
    public PricingTierDTO createPricingTier(PricingTierRequestDTO request) {
        PricingTierEntity entity = PricingTierMapper.INSTANCE.toCreateEntity(request);
        
        if (entity.getCurrency() == null) {
            entity.setCurrency("EUR");
        }
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        
        return PricingTierMapper.INSTANCE.toDto(pricingTierRepository.save(entity));
    }

    @Override
    @Transactional
    public PricingTierDTO updatePricingTier(Long id, PricingTierRequestDTO request) {
        PricingTierEntity existingEntity = findPricingTierById(id);
        
        PricingTierMapper.INSTANCE.updateEntity(request, existingEntity);
        
        return PricingTierMapper.INSTANCE.toDto(pricingTierRepository.save(existingEntity));
    }

    @Override
    @Transactional
    public void deletePricingTier(Long id) {
        if (!pricingTierRepository.existsById(id)) {
            throw new RdXException(HttpStatus.NOT_FOUND,
                    "Pricing tier not found with id: " + id, "PRICING_TIER_NOT_FOUND");
        }
        pricingTierRepository.deleteById(id);
    }

    private PricingTierEntity findPricingTierById(Long id) {
        return pricingTierRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "Pricing tier not found with id: " + id, "PRICING_TIER_NOT_FOUND"));
    }
}
