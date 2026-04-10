package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationRequestDTO;
import com.reddiax.rdxvideo.model.dto.TvDeviceRegistrationResponseDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.TvDeviceRegistrationEntity;
import com.reddiax.rdxvideo.repository.OrganizationRepository;
import com.reddiax.rdxvideo.repository.TvDeviceRegistrationRepository;
import com.reddiax.rdxvideo.security.CryptoUtils;
import com.reddiax.rdxvideo.service.TvDeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * Implementation of TV device registration service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TvDeviceRegistrationServiceImpl implements TvDeviceRegistrationService {
    
    private final TvDeviceRegistrationRepository deviceRepository;
    private final OrganizationRepository organizationRepository;
    private final CryptoUtils cryptoUtils;
    
    @Override
    @Transactional
    public TvDeviceRegistrationResponseDTO registerDevice(TvDeviceRegistrationRequestDTO request) {
        log.info("TV device registration request for deviceId: {}, organizationId: {}", 
                request.getDeviceId(), request.getOrganizationId());
        
        OrganizationEntity organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Organization not found: " + request.getOrganizationId(), "ORGANIZATION_NOT_FOUND"));
        
        // Check if device is already registered
        var existingDevice = deviceRepository.findByDeviceId(request.getDeviceId());
        
        if (existingDevice.isPresent()) {
            // Update existing registration
            TvDeviceRegistrationEntity device = existingDevice.get();
            
            // Verify device belongs to the same organization
            if (!device.getOrganization().getId().equals(request.getOrganizationId())) {
                throw new RdXException(HttpStatus.CONFLICT, 
                        "Device is registered to a different organization", "DEVICE_CONFLICT");
            }
            
            device.setPublicKey(request.getPublicKey());
            device.setDeviceName(request.getDeviceName());
            device.setDeviceModel(request.getDeviceModel());
            device.setDeviceManufacturer(request.getDeviceManufacturer());
            device.setOsVersion(request.getOsVersion());
            device.setAppVersion(request.getAppVersion());
            device.setActive(true);
            device.updateLastSeen();
            
            deviceRepository.save(device);
            
            log.info("Updated TV device registration for org {} device {}", 
                    request.getOrganizationId(), request.getDeviceId());
            
            return TvDeviceRegistrationResponseDTO.builder()
                    .id(device.getId())
                    .deviceToken(device.getDeviceToken())
                    .serverPublicKey(cryptoUtils.getServerPublicKeyBase64())
                    .organizationId(organization.getId())
                    .isRegistered(true)
                    .message("TV device registration updated")
                    .build();
        }
        
        // Create new registration
        String deviceToken = cryptoUtils.generateDeviceToken();
        
        // Ensure device token is unique
        while (deviceRepository.existsByDeviceToken(deviceToken)) {
            deviceToken = cryptoUtils.generateDeviceToken();
        }
        
        TvDeviceRegistrationEntity device = TvDeviceRegistrationEntity.builder()
                .organization(organization)
                .deviceId(request.getDeviceId())
                .publicKey(request.getPublicKey())
                .deviceToken(deviceToken)
                .deviceName(request.getDeviceName())
                .deviceModel(request.getDeviceModel())
                .deviceManufacturer(request.getDeviceManufacturer())
                .osVersion(request.getOsVersion())
                .appVersion(request.getAppVersion())
                .build();
        
        device = deviceRepository.save(device);
        
        log.info("Registered new TV device for org {} device {}", 
                request.getOrganizationId(), request.getDeviceId());
        
        return TvDeviceRegistrationResponseDTO.builder()
                .id(device.getId())
                .deviceToken(device.getDeviceToken())
                .serverPublicKey(cryptoUtils.getServerPublicKeyBase64())
                .organizationId(organization.getId())
                .isRegistered(true)
                .message("TV device registered successfully")
                .build();
    }
    
    @Override
    @Transactional
    public TvDeviceRegistrationDTO getDeviceByToken(String deviceToken, Boolean foreground) {
        TvDeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "TV device not found", "DEVICE_NOT_FOUND"));
        if (Boolean.TRUE.equals(foreground)) {
            device.setLastForegroundAt(LocalDateTime.now());
            device.updateLastSeen();
            deviceRepository.save(device);
        }
        return toDTO(device);
    }
    
    @Override
    public List<TvDeviceRegistrationDTO> getAllDevices(Long organizationId) {
        if (organizationId != null) {
            return getDevicesByOrganization(organizationId);
        }
        return deviceRepository.findAllByActiveTrueOrderByLastSeenAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TvDeviceRegistrationDTO> getDevicesByOrganization(Long organizationId) {
        return deviceRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long validateDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) {
            log.warn("Device token validation failed: token is null or blank");
            throw new RdXException(HttpStatus.UNAUTHORIZED, 
                    "Device token is required", "DEVICE_TOKEN_REQUIRED");
        }
        
        TvDeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> {
                    log.warn("Device token validation failed: device not found for token: {}", 
                            deviceToken.substring(0, Math.min(8, deviceToken.length())) + "...");
                    return new RdXException(HttpStatus.UNAUTHORIZED, 
                            "Invalid device token", "INVALID_DEVICE_TOKEN");
                });
        
        if (!device.getActive()) {
            log.warn("Device token validation failed: device {} is deactivated", device.getDeviceId());
            throw new RdXException(HttpStatus.UNAUTHORIZED, 
                    "Device is deactivated", "DEVICE_DEACTIVATED");
        }
        
        // Log trust status but don't block (trust system is advisory for now)
        if (!device.getIsTrusted()) {
            log.warn("Device {} has low trust score: {} (threshold: 50)", 
                    device.getDeviceId(), device.getTrustScore());
            // Continue anyway - trust system will be enforced later
        }
        
        // Update last seen
        device.updateLastSeen();
        deviceRepository.save(device);
        
        log.debug("Device token validated for org {} device {}", 
                device.getOrganization().getId(), device.getDeviceId());
        
        return device.getOrganization().getId();
    }
    
    @Override
    @Transactional
    public void updateTrustScore(Long deviceId, int scoreChange, String reason) {
        TvDeviceRegistrationEntity device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "TV device not found", "DEVICE_NOT_FOUND"));
        
        int oldScore = device.getTrustScore();
        
        if (scoreChange < 0) {
            device.decreaseTrustScore(Math.abs(scoreChange));
        } else {
            device.increaseTrustScore(scoreChange);
        }
        
        deviceRepository.save(device);
        
        log.info("Updated trust score for TV device {}: {} -> {} (reason: {})", 
                deviceId, oldScore, device.getTrustScore(), reason);
        
        if (!device.getIsTrusted()) {
            log.warn("TV device {} is now untrusted (score: {})", deviceId, device.getTrustScore());
        }
    }
    
    @Override
    public boolean isDeviceTrusted(String deviceToken) {
        return deviceRepository.findByDeviceToken(deviceToken)
                .map(device -> device.getActive() && device.getIsTrusted())
                .orElse(false);
    }
    
    @Override
    @Transactional
    public void unregisterDevice(String deviceId, Long organizationId) {
        TvDeviceRegistrationEntity device = deviceRepository.findByOrganizationIdAndDeviceId(organizationId, deviceId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "TV device not found", "DEVICE_NOT_FOUND"));
        
        device.setActive(false);
        deviceRepository.save(device);
        
        log.info("Unregistered TV device {} for organization {}", deviceId, organizationId);
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
    public boolean verifyDeviceSignature(String deviceToken, String data, String signature) {
        TvDeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "TV device not found", "DEVICE_NOT_FOUND"));
        
        if (!device.getActive() || !device.getIsTrusted()) {
            log.warn("Signature verification attempted for inactive/untrusted device: {}", deviceToken);
            return false;
        }
        
        boolean isValid = cryptoUtils.verifySignature(
                device.getPublicKey(), 
                data.getBytes(StandardCharsets.UTF_8), 
                signature);
        
        if (!isValid) {
            log.warn("Invalid signature from TV device: {}", device.getDeviceId());
            updateTrustScore(device.getId(), -10, "Invalid signature");
        } else {
            updateTrustScore(device.getId(), 1, "Valid signature");
        }
        
        return isValid;
    }

    @Override
    @Transactional
    public void requestLaunch(String deviceId, Long organizationId) {
        TvDeviceRegistrationEntity device = deviceRepository.findByOrganizationIdAndDeviceId(organizationId, deviceId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "TV device not found", "DEVICE_NOT_FOUND"));
        if (!device.getActive()) {
            throw new RdXException(HttpStatus.BAD_REQUEST, "Device is inactive", "DEVICE_INACTIVE");
        }
        device.setLaunchRequestedAt(LocalDateTime.now());
        deviceRepository.save(device);
        log.info("Launch requested for TV device {} (org {})", deviceId, organizationId);
    }

    @Override
    @Transactional
    public void clearLaunchRequest(String deviceToken) {
        deviceRepository.findByDeviceToken(deviceToken).ifPresent(device -> {
            device.setLaunchRequestedAt(null);
            deviceRepository.save(device);
            log.debug("Launch request cleared for device {}", device.getDeviceId());
        });
    }

    @Override
    @Transactional
    public void updateDisplayConnected(String deviceToken, boolean connected) {
        deviceRepository.findByDeviceToken(deviceToken).ifPresent(device -> {
            device.setDisplayConnected(connected);
            deviceRepository.save(device);
            log.debug("TV device {} display connected: {}", device.getDeviceId(), connected);
        });
    }

    /** Consider app "active" if last foreground report was within this many minutes. */
    private static final int APP_ACTIVE_THRESHOLD_MINUTES = 10;

    private TvDeviceRegistrationDTO toDTO(TvDeviceRegistrationEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minus(APP_ACTIVE_THRESHOLD_MINUTES, MINUTES);
        boolean fromForeground = entity.getLastForegroundAt() != null
                && !entity.getLastForegroundAt().isBefore(cutoff);
        // Fallback: ako aplikacija još ne šalje foreground=true (starija verzija), smatraj aktivnom kad je lastSeenAt nedavno
        boolean fromLastSeen = entity.getLastForegroundAt() == null
                && entity.getLastSeenAt() != null
                && !entity.getLastSeenAt().isBefore(cutoff);
        boolean appActive = fromForeground || fromLastSeen;
        return TvDeviceRegistrationDTO.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization().getId())
                .organizationName(entity.getOrganization().getName())
                .deviceId(entity.getDeviceId())
                .deviceToken(entity.getDeviceToken())
                .deviceName(entity.getDeviceName())
                .deviceModel(entity.getDeviceModel())
                .deviceManufacturer(entity.getDeviceManufacturer())
                .osVersion(entity.getOsVersion())
                .appVersion(entity.getAppVersion())
                .registeredAt(entity.getRegisteredAt())
                .lastSeenAt(entity.getLastSeenAt())
                .isTrusted(entity.getIsTrusted())
                .trustScore(entity.getTrustScore())
                .active(entity.getActive())
                .appActive(appActive)
                .launchRequestedAt(entity.getLaunchRequestedAt())
                .displayConnected(entity.getDisplayConnected())
                .build();
    }
}
