package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.DeviceRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistrationEntity, Long> {
    
    Optional<DeviceRegistrationEntity> findByDeviceId(String deviceId);
    
    Optional<DeviceRegistrationEntity> findByDeviceToken(String deviceToken);
    
    List<DeviceRegistrationEntity> findByUserId(Long userId);
    
    @Query("SELECT d FROM DeviceRegistrationEntity d WHERE d.user.externalId = :externalId")
    List<DeviceRegistrationEntity> findByUserExternalId(@Param("externalId") String externalId);
    
    @Query("SELECT d FROM DeviceRegistrationEntity d WHERE d.user.externalId = :externalId AND d.deviceId = :deviceId")
    Optional<DeviceRegistrationEntity> findByUserExternalIdAndDeviceId(
            @Param("externalId") String externalId,
            @Param("deviceId") String deviceId);
    
    boolean existsByDeviceId(String deviceId);
    
    boolean existsByDeviceToken(String deviceToken);
    
    @Query("SELECT d FROM DeviceRegistrationEntity d WHERE d.isTrusted = false")
    List<DeviceRegistrationEntity> findUntrustedDevices();
    
    @Query("SELECT d FROM DeviceRegistrationEntity d WHERE d.trustScore < :threshold")
    List<DeviceRegistrationEntity> findDevicesWithLowTrustScore(@Param("threshold") int threshold);
    
    @Query("SELECT d.fcmToken FROM DeviceRegistrationEntity d WHERE d.user.id = :userId AND d.fcmToken IS NOT NULL")
    List<String> findFcmTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT d.fcmToken FROM DeviceRegistrationEntity d WHERE d.fcmToken IS NOT NULL AND d.user.status = 'ACTIVE'")
    List<String> findAllActiveFcmTokens();

    @Query("SELECT COUNT(DISTINCT d.fcmToken) FROM DeviceRegistrationEntity d WHERE d.fcmToken IS NOT NULL AND d.user.status = 'ACTIVE'")
    long countActiveFcmTokens();
}
