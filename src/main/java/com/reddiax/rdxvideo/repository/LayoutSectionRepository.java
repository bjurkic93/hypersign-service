package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.LayoutSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LayoutSectionRepository extends JpaRepository<LayoutSectionEntity, Long> {
    List<LayoutSectionEntity> findByLayout_IdOrderByZIndexAsc(Long layoutId);
    
    void deleteByLayout_Id(Long layoutId);
}
