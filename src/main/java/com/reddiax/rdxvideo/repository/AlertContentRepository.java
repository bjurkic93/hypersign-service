package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.AlertContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertContentRepository extends JpaRepository<AlertContentEntity, Long> {
    List<AlertContentEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<AlertContentEntity> findAllByOrderByCreatedAtDesc();
}
