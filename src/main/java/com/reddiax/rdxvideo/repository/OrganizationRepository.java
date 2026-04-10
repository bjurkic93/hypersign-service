package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    Optional<OrganizationEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT o.logoImageId FROM OrganizationEntity o WHERE o.logoImageId IS NOT NULL")
    Set<Long> findAllLogoImageIds();
}
