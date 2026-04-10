package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.TickerContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TickerContentRepository extends JpaRepository<TickerContentEntity, Long> {
    List<TickerContentEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<TickerContentEntity> findAllByOrderByCreatedAtDesc();
}
