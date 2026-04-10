package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.PricingTierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingTierRepository extends JpaRepository<PricingTierEntity, Long> {

    Page<PricingTierEntity> findByActiveTrue(Pageable pageable);

    List<PricingTierEntity> findByActiveTrue();
}
