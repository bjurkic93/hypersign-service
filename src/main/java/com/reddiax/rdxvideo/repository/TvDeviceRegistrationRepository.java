package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.TvDeviceRegistrationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TvDeviceRegistrationRepository extends JpaRepository<TvDeviceRegistrationEntity, Long> {

    Optional<TvDeviceRegistrationEntity> findByDeviceToken(String deviceToken);

    Optional<TvDeviceRegistrationEntity> findByDeviceId(String deviceId);

    Optional<TvDeviceRegistrationEntity> findByDeviceIdAndActiveTrue(String deviceId);

    @Query("SELECT d FROM TvDeviceRegistrationEntity d WHERE d.organization.id = :organizationId AND d.active = true")
    List<TvDeviceRegistrationEntity> findByOrganizationIdAndActiveTrue(@Param("organizationId") Long organizationId);

    @Query("SELECT d FROM TvDeviceRegistrationEntity d WHERE d.organization.id = :organizationId AND d.deviceId = :deviceId")
    Optional<TvDeviceRegistrationEntity> findByOrganizationIdAndDeviceId(
            @Param("organizationId") Long organizationId, 
            @Param("deviceId") String deviceId);

    Optional<TvDeviceRegistrationEntity> findByDeviceIdAndOrganization_Id(String deviceId, Long organizationId);

    boolean existsByDeviceToken(String deviceToken);

    boolean existsByDeviceId(String deviceId);

    @Query("SELECT d FROM TvDeviceRegistrationEntity d WHERE d.deviceToken = :deviceToken AND d.active = true AND d.isTrusted = true")
    Optional<TvDeviceRegistrationEntity> findTrustedByDeviceToken(@Param("deviceToken") String deviceToken);

    long countByActiveTrue();

    List<TvDeviceRegistrationEntity> findByActiveTrueOrderByLastSeenAtDesc(Pageable pageable);

    List<TvDeviceRegistrationEntity> findAllByActiveTrueOrderByLastSeenAtDesc();
}
