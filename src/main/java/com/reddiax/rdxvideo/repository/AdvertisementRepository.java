package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import com.reddiax.rdxvideo.model.entity.AdvertisementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdvertisementRepository extends JpaRepository<AdvertisementEntity, Long> {

    Page<AdvertisementEntity> findByActiveTrue(Pageable pageable);

    Page<AdvertisementEntity> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<AdvertisementEntity> findByPlatform(AdvertisementPlatformEnum platform, Pageable pageable);

    Page<AdvertisementEntity> findByIsPublicTrue(Pageable pageable);

    @Query("SELECT a FROM AdvertisementEntity a WHERE a.active = true " +
           "AND a.validFrom <= :now AND a.validTo >= :now")
    Page<AdvertisementEntity> findValidAdvertisements(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT a FROM AdvertisementEntity a WHERE a.active = true " +
           "AND a.platform = :platform " +
           "AND a.validFrom <= :now AND a.validTo >= :now " +
           "AND (a.isPublic = true OR a.organizationId = :organizationId)")
    Page<AdvertisementEntity> findValidAdvertisementsByPlatformAndOrganization(
            @Param("platform") AdvertisementPlatformEnum platform,
            @Param("organizationId") Long organizationId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM AdvertisementEntity a WHERE a.platform = :platform AND a.active = true")
    long countByPlatformAndActiveTrue(@Param("platform") AdvertisementPlatformEnum platform);

    @Query("SELECT a FROM AdvertisementEntity a WHERE a.platform = :platform ORDER BY a.createdAt DESC")
    List<AdvertisementEntity> findByPlatformOrderByCreatedAtDesc(@Param("platform") AdvertisementPlatformEnum platform, Pageable pageable);
}
