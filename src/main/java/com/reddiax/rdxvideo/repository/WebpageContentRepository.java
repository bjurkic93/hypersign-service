package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.WebpageContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebpageContentRepository extends JpaRepository<WebpageContentEntity, Long> {
    List<WebpageContentEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<WebpageContentEntity> findAllByOrderByCreatedAtDesc();
}
