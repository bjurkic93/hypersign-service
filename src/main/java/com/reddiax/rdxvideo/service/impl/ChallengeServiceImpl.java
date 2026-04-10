package com.reddiax.rdxvideo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.ChallengeDTO;
import com.reddiax.rdxvideo.model.dto.ChallengeResponseRequestDTO;
import com.reddiax.rdxvideo.model.dto.ChallengeResponseResultDTO;
import com.reddiax.rdxvideo.model.entity.DeviceRegistrationEntity;
import com.reddiax.rdxvideo.model.entity.UserAdvertisementEntity;
import com.reddiax.rdxvideo.model.entity.VerificationChallengeEntity;
import com.reddiax.rdxvideo.repository.AdvertisementImageRepository;
import com.reddiax.rdxvideo.repository.DeviceRegistrationRepository;
import com.reddiax.rdxvideo.repository.UserAdvertisementRepository;
import com.reddiax.rdxvideo.repository.VerificationChallengeRepository;
import com.reddiax.rdxvideo.security.CryptoUtils;
import com.reddiax.rdxvideo.service.ChallengeService;
import com.reddiax.rdxvideo.service.DeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeServiceImpl implements ChallengeService {
    
    private static final int CHALLENGE_VALIDITY_MINUTES = 30;
    private static final int TRUST_SCORE_PENALTY_FAILED_CHALLENGE = 10;
    private static final int TRUST_SCORE_PENALTY_HASH_MISMATCH = 20;
    private static final int TRUST_SCORE_PENALTY_INVALID_ATTESTATION = 50;
    private static final int TRUST_SCORE_REWARD_SUCCESS = 1;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final VerificationChallengeRepository challengeRepository;
    private final DeviceRegistrationRepository deviceRepository;
    private final UserAdvertisementRepository userAdvertisementRepository;
    private final AdvertisementImageRepository advertisementImageRepository;
    private final DeviceRegistrationService deviceRegistrationService;
    private final CryptoUtils cryptoUtils;
    
    @Override
    @Transactional
    public ChallengeDTO generateChallenge(String deviceToken) {
        DeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Device not found", "DEVICE_NOT_FOUND"));
        
        // Get active user advertisement for this user
        UserAdvertisementEntity userAd = userAdvertisementRepository
                .findActiveByUserExternalId(device.getUser().getExternalId())
                .orElse(null);
        
        String expectedHash = null;
        if (userAd != null && userAd.getAdvertisement() != null) {
            // Get the expected wallpaper hash from advertisement images
            var images = advertisementImageRepository.findByAdvertisementIdOrderByDisplayOrderAsc(
                    userAd.getAdvertisement().getId());
            if (images != null && !images.isEmpty()) {
                expectedHash = images.get(0).getImageHash();
            }
        }
        
        // Generate challenge data
        String nonce = cryptoUtils.generateNonce();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(CHALLENGE_VALIDITY_MINUTES);
        
        Map<String, Object> challengeData = new HashMap<>();
        challengeData.put("nonce", nonce);
        challengeData.put("timestamp", System.currentTimeMillis());
        challengeData.put("deviceId", device.getDeviceId());
        if (expectedHash != null) {
            challengeData.put("expectedHash", expectedHash);
        }
        
        String challengeJson;
        try {
            challengeJson = objectMapper.writeValueAsString(challengeData);
        } catch (Exception e) {
            log.error("Failed to serialize challenge data", e);
            throw new RdXException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to generate challenge", "CHALLENGE_GENERATION_FAILED");
        }
        
        // Create challenge entity
        VerificationChallengeEntity challenge = VerificationChallengeEntity.builder()
                .device(device)
                .userAdvertisement(userAd)
                .nonce(nonce)
                .challengeData(challengeJson)
                .expectedWallpaperHash(expectedHash)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();
        
        challenge = challengeRepository.save(challenge);
        
        log.debug("Generated challenge {} for device {}", challenge.getId(), deviceToken);
        
        // Update device last seen
        deviceRegistrationService.updateLastSeen(deviceToken);
        
        return ChallengeDTO.builder()
                .id(challenge.getId())
                .nonce(nonce)
                .encryptedChallengeData(challengeJson) // In production, encrypt this with device public key
                .issuedAt(now)
                .expiresAt(expiresAt)
                .userAdvertisementId(userAd != null ? userAd.getId() : null)
                .build();
    }
    
    @Override
    @Transactional
    public ChallengeDTO getCurrentChallenge(String deviceToken) {
        // Verify device exists
        if (!deviceRepository.existsByDeviceToken(deviceToken)) {
            throw new RdXException(HttpStatus.NOT_FOUND, "Device not found", "DEVICE_NOT_FOUND");
        }
        
        // Check for existing pending challenge
        var pendingChallenge = challengeRepository.findLatestPendingChallengeByDeviceToken(
                deviceToken, LocalDateTime.now());
        
        if (pendingChallenge.isPresent()) {
            VerificationChallengeEntity challenge = pendingChallenge.get();
            return ChallengeDTO.builder()
                    .id(challenge.getId())
                    .nonce(challenge.getNonce())
                    .encryptedChallengeData(challenge.getChallengeData())
                    .issuedAt(challenge.getIssuedAt())
                    .expiresAt(challenge.getExpiresAt())
                    .userAdvertisementId(challenge.getUserAdvertisement() != null ? 
                            challenge.getUserAdvertisement().getId() : null)
                    .build();
        }
        
        // Generate new challenge
        return generateChallenge(deviceToken);
    }
    
    @Override
    @Transactional
    public ChallengeResponseResultDTO processResponse(Long challengeId, ChallengeResponseRequestDTO response) {
        VerificationChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Challenge not found", "CHALLENGE_NOT_FOUND"));
        
        // Validate challenge state
        if (challenge.isExpired()) {
            throw new RdXException(HttpStatus.BAD_REQUEST, 
                    "Challenge has expired", "CHALLENGE_EXPIRED");
        }
        
        if (challenge.isResponded()) {
            throw new RdXException(HttpStatus.BAD_REQUEST, 
                    "Challenge already responded", "CHALLENGE_ALREADY_RESPONDED");
        }
        
        // Verify nonce matches
        if (!challenge.getNonce().equals(response.getNonce())) {
            throw new RdXException(HttpStatus.BAD_REQUEST, 
                    "Nonce mismatch", "NONCE_MISMATCH");
        }
        
        DeviceRegistrationEntity device = challenge.getDevice();
        
        // Verify signature
        String dataToVerify = response.getNonce() + response.getWallpaperHash() + response.getTimestamp();
        boolean signatureValid = verifySignature(
                device.getDeviceToken(), 
                dataToVerify.getBytes(StandardCharsets.UTF_8), 
                response.getSignature());
        
        // Verify wallpaper hash
        boolean hashMatch = false;
        if (challenge.getExpectedWallpaperHash() != null) {
            hashMatch = challenge.getExpectedWallpaperHash().equals(response.getWallpaperHash());
        } else {
            // No expected hash, just accept the response
            hashMatch = true;
        }
        
        // Verify attestation (if provided)
        boolean attestationValid = true;
        if (response.getDeviceAttestationToken() != null) {
            // TODO: Implement Play Integrity API verification
            attestationValid = true; // For now, accept all attestations
        }
        
        // Update challenge with response
        challenge.setRespondedAt(LocalDateTime.now());
        challenge.setResponseSignature(response.getSignature());
        challenge.setResponseWallpaperHash(response.getWallpaperHash());
        challenge.setResponseValid(signatureValid);
        challenge.setWallpaperHashMatch(hashMatch);
        challenge.setDeviceAttestationToken(response.getDeviceAttestationToken());
        challenge.setAttestationValid(attestationValid);
        
        challengeRepository.save(challenge);
        
        // Update trust score based on results
        boolean overallValid = signatureValid && hashMatch && attestationValid;
        
        if (!signatureValid) {
            deviceRegistrationService.updateTrustScore(device.getId(), 
                    -TRUST_SCORE_PENALTY_FAILED_CHALLENGE, "Invalid signature");
        }
        
        if (!hashMatch) {
            deviceRegistrationService.updateTrustScore(device.getId(), 
                    -TRUST_SCORE_PENALTY_HASH_MISMATCH, "Wallpaper hash mismatch");
        }
        
        if (!attestationValid) {
            deviceRegistrationService.updateTrustScore(device.getId(), 
                    -TRUST_SCORE_PENALTY_INVALID_ATTESTATION, "Invalid device attestation");
        }
        
        if (overallValid) {
            deviceRegistrationService.updateTrustScore(device.getId(), 
                    TRUST_SCORE_REWARD_SUCCESS, "Successful challenge response");
        }
        
        // Refresh device to get updated trust score
        device = deviceRepository.findById(device.getId()).orElse(device);
        
        log.info("Processed challenge {} response - valid: {}, hashMatch: {}, attestation: {}", 
                challengeId, signatureValid, hashMatch, attestationValid);
        
        return ChallengeResponseResultDTO.builder()
                .challengeId(challengeId)
                .signatureValid(signatureValid)
                .wallpaperHashMatch(hashMatch)
                .attestationValid(attestationValid)
                .overallValid(overallValid)
                .newTrustScore(device.getTrustScore())
                .message(overallValid ? "Challenge verified successfully" : "Challenge verification failed")
                .build();
    }
    
    @Override
    public boolean verifySignature(String deviceToken, byte[] data, String signature) {
        DeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElse(null);
        
        if (device == null) {
            return false;
        }
        
        return cryptoUtils.verifySignature(device.getPublicKey(), data, signature);
    }
    
    @Override
    @Transactional
    @Scheduled(cron = "0 0 */6 * * *") // Run every 6 hours
    public void cleanupExpiredChallenges() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        challengeRepository.deleteExpiredChallenges(cutoff);
        log.info("Cleaned up expired challenges older than {}", cutoff);
    }
}
