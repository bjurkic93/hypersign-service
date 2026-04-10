package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.RssContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RssContentRepository extends JpaRepository<RssContentEntity, Long> {
    List<RssContentEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<RssContentEntity> findAllByOrderByCreatedAtDesc();
}
