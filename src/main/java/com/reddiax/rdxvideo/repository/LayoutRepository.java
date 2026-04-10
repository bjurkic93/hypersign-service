package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.LayoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LayoutRepository extends JpaRepository<LayoutEntity, Long> {
    List<LayoutEntity> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);
    
    List<LayoutEntity> findAllByOrderByCreatedAtDesc();
}
