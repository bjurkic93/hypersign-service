package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.DeviceRegistrationDTO;
import com.reddiax.rdxvideo.model.dto.DeviceRegistrationRequestDTO;
import com.reddiax.rdxvideo.model.dto.DeviceRegistrationResponseDTO;
import com.reddiax.rdxvideo.model.entity.DeviceRegistrationEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.repository.DeviceRegistrationRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.CryptoUtils;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceRegistrationServiceImpl implements DeviceRegistrationService {
    
    private final DeviceRegistrationRepository deviceRepository;
    private final UserRepository userRepository;
    private final CryptoUtils cryptoUtils;
    
    @Override
    @Transactional
    public DeviceRegistrationResponseDTO registerDevice(DeviceRegistrationRequestDTO request) {
        String externalId = getCurrentUserExternalId();
        
        UserEntity user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "User not found", "USER_NOT_FOUND"));
        
        // Check if device is already registered
        var existingDevice = deviceRepository.findByUserExternalIdAndDeviceId(externalId, request.getDeviceId());
        
        if (existingDevice.isPresent()) {
            // Update existing registration
            DeviceRegistrationEntity device = existingDevice.get();
            device.setPublicKey(request.getPublicKey());
            device.setDeviceModel(request.getDeviceModel());
            device.setDeviceManufacturer(request.getDeviceManufacturer());
            device.setOsVersion(request.getOsVersion());
            device.setAppVersion(request.getAppVersion());
            if (request.getFcmToken() != null) {
                device.setFcmToken(request.getFcmToken());
            }
            device.updateLastSeen();
            
            deviceRepository.save(device);
            
            log.info("Updated device registration for user {} device {}", externalId, request.getDeviceId());
            
            return DeviceRegistrationResponseDTO.builder()
                    .id(device.getId())
                    .deviceToken(device.getDeviceToken())
                    .serverPublicKey(cryptoUtils.getServerPublicKeyBase64())
                    .isRegistered(true)
                    .message("Device registration updated")
                    .build();
        }
        
        // Create new registration
        String deviceToken = cryptoUtils.generateDeviceToken();
        
        // Ensure device token is unique
        while (deviceRepository.existsByDeviceToken(deviceToken)) {
            deviceToken = cryptoUtils.generateDeviceToken();
        }
        
        DeviceRegistrationEntity device = DeviceRegistrationEntity.builder()
                .user(user)
                .deviceId(request.getDeviceId())
                .publicKey(request.getPublicKey())
                .deviceToken(deviceToken)
                .deviceModel(request.getDeviceModel())
                .deviceManufacturer(request.getDeviceManufacturer())
                .osVersion(request.getOsVersion())
                .appVersion(request.getAppVersion())
                .fcmToken(request.getFcmToken())
                .build();
        
        device = deviceRepository.save(device);
        
        log.info("Registered new device for user {} device {}", externalId, request.getDeviceId());
        
        return DeviceRegistrationResponseDTO.builder()
                .id(device.getId())
                .deviceToken(device.getDeviceToken())
                .serverPublicKey(cryptoUtils.getServerPublicKeyBase64())
                .isRegistered(true)
                .message("Device registered successfully")
                .build();
    }
    
    @Override
    public DeviceRegistrationDTO getDeviceByToken(String deviceToken) {
        DeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Device not found", "DEVICE_NOT_FOUND"));
        
        return toDTO(device);
    }
    
    @Override
    public List<DeviceRegistrationDTO> getMyDevices() {
        String externalId = getCurrentUserExternalId();
        return deviceRepository.findByUserExternalId(externalId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateTrustScore(Long deviceId, int scoreChange, String reason) {
        DeviceRegistrationEntity device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Device not found", "DEVICE_NOT_FOUND"));
        
        int oldScore = device.getTrustScore();
        
        if (scoreChange < 0) {
            device.decreaseTrustScore(Math.abs(scoreChange));
        } else {
            device.increaseTrustScore(scoreChange);
        }
        
        deviceRepository.save(device);
        
        log.info("Updated trust score for device {}: {} -> {} (reason: {})", 
                deviceId, oldScore, device.getTrustScore(), reason);
        
        if (!device.getIsTrusted()) {
            log.warn("Device {} is now untrusted (score: {})", deviceId, device.getTrustScore());
        }
    }
    
    @Override
    public boolean isDeviceTrusted(String deviceToken) {
        return deviceRepository.findByDeviceToken(deviceToken)
                .map(DeviceRegistrationEntity::getIsTrusted)
                .orElse(false);
    }
    
    @Override
    @Transactional
    public void unregisterDevice(String deviceId) {
        String externalId = getCurrentUserExternalId();
        
        DeviceRegistrationEntity device = deviceRepository.findByUserExternalIdAndDeviceId(externalId, deviceId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Device not found", "DEVICE_NOT_FOUND"));
        
        deviceRepository.delete(device);
        
        log.info("Unregistered device {} for user {}", deviceId, externalId);
    }
    
    @Override
    @Transactional
    public void updateLastSeen(String deviceToken) {
        deviceRepository.findByDeviceToken(deviceToken)
                .ifPresent(device -> {
                    device.updateLastSeen();
                    deviceRepository.save(device);
                });
    }
    
    @Override
    @Transactional
    public void updateFcmToken(String deviceToken, String fcmToken) {
        DeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Device not found", "DEVICE_NOT_FOUND"));
        
        device.setFcmToken(fcmToken);
        device.updateLastSeen();
        deviceRepository.save(device);
        
        log.info("Updated FCM token for device {} (user {})", 
                device.getDeviceId(), device.getUser().getId());
    }
    
    private String getCurrentUserExternalId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RdXException(HttpStatus.UNAUTHORIZED, 
                        "User not authenticated", "UNAUTHORIZED"));
    }
    
    private DeviceRegistrationDTO toDTO(DeviceRegistrationEntity entity) {
        return DeviceRegistrationDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .deviceId(entity.getDeviceId())
                .deviceToken(entity.getDeviceToken())
                .deviceModel(entity.getDeviceModel())
                .deviceManufacturer(entity.getDeviceManufacturer())
                .osVersion(entity.getOsVersion())
                .appVersion(entity.getAppVersion())
                .registeredAt(entity.getRegisteredAt())
                .lastSeenAt(entity.getLastSeenAt())
                .isTrusted(entity.getIsTrusted())
                .trustScore(entity.getTrustScore())
                .build();
    }
}
