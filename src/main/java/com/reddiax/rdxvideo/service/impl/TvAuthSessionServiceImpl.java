package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.model.entity.*;
import com.reddiax.rdxvideo.repository.OrganizationRepository;
import com.reddiax.rdxvideo.repository.TvAuthSessionRepository;
import com.reddiax.rdxvideo.repository.TvDeviceRegistrationRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.TvAuthSessionService;
import com.reddiax.rdxvideo.service.TvDeviceRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TvAuthSessionServiceImpl implements TvAuthSessionService {

    private final TvAuthSessionRepository sessionRepository;
    private final TvDeviceRegistrationRepository deviceRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TvDeviceRegistrationService deviceRegistrationService;

    @Value("${app.frontend.url:https://hypersign.hyperbluex.com}")
    private String frontendUrl;

    private static final int SESSION_EXPIRY_MINUTES = 60;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public TvAuthSessionDTO createSession(TvAuthCreateRequest request) {
        log.info("Creating TV auth session for device: {}", request.getDeviceId());

        // Check if device already has an active session
        sessionRepository.findActiveSessionByDeviceId(request.getDeviceId(), LocalDateTime.now())
                .ifPresent(existing -> {
                    log.info("Expiring existing session for device: {}", request.getDeviceId());
                    existing.setStatus(TvAuthSessionStatus.EXPIRED);
                    sessionRepository.save(existing);
                });

        // Generate unique session ID and code
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String sessionCode = generateSessionCode();

        TvAuthSessionEntity session = TvAuthSessionEntity.builder()
                .sessionId(sessionId)
                .sessionCode(sessionCode)
                .deviceId(request.getDeviceId())
                .status(TvAuthSessionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(SESSION_EXPIRY_MINUTES))
                .build();

        session = sessionRepository.save(session);
        log.info("Created TV auth session: {} with code: {}", sessionId, sessionCode);

        return mapToDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public TvAuthSessionDTO getSessionStatus(String sessionId) {
        TvAuthSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Session not found", "SESSION_NOT_FOUND"));

        // Check expiry
        if (session.getStatus() == TvAuthSessionStatus.PENDING && session.isExpired()) {
            session.setStatus(TvAuthSessionStatus.EXPIRED);
            sessionRepository.save(session);
        }

        return mapToDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public TvAuthSessionDTO getSessionForApproval(String sessionIdOrCode) {
        TvAuthSessionEntity session = findSession(sessionIdOrCode);

        log.info("Checking session for approval: id={}, status={}, expiresAt={}, now={}", 
                session.getSessionId(), session.getStatus(), session.getExpiresAt(), LocalDateTime.now());

        if (session.isExpired()) {
            log.warn("Session expired: expiresAt={}, now={}", session.getExpiresAt(), LocalDateTime.now());
            throw new RdXException(HttpStatus.GONE, "Session has expired", "SESSION_EXPIRED");
        }

        if (session.getStatus() != TvAuthSessionStatus.PENDING) {
            throw new RdXException(HttpStatus.CONFLICT, "Session already " + session.getStatus().name().toLowerCase(), "SESSION_NOT_PENDING");
        }

        return mapToDTO(session);
    }

    @Override
    @Transactional
    public TvAuthSessionDTO approveSession(String sessionIdOrCode, TvAuthApproveRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RdXException(HttpStatus.UNAUTHORIZED, "User not authenticated", "UNAUTHORIZED"));

        UserEntity user = userRepository.findByExternalId(currentUserId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND"));

        TvAuthSessionEntity session = findSession(sessionIdOrCode);

        if (!session.canBeApproved()) {
            if (session.isExpired()) {
                throw new RdXException(HttpStatus.GONE, "Session has expired", "SESSION_EXPIRED");
            }
            throw new RdXException(HttpStatus.CONFLICT, "Session cannot be approved", "SESSION_NOT_PENDING");
        }

        OrganizationEntity organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Organization not found", "ORG_NOT_FOUND"));

        // Register or update the TV device
        String deviceToken = registerOrUpdateDevice(session, organization);

        // Update session
        session.setStatus(TvAuthSessionStatus.APPROVED);
        session.setApprovedByUser(user);
        session.setOrganization(organization);
        session.setApprovedAt(LocalDateTime.now());
        session.setAccessToken(deviceToken); // Using device token as access token for TV

        session = sessionRepository.save(session);
        log.info("Approved TV auth session: {} for org: {} by user: {}", 
                session.getSessionId(), organization.getId(), currentUserId);

        return mapToDTO(session);
    }

    @Override
    @Transactional
    public void markSessionUsed(String sessionId) {
        TvAuthSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Session not found", "SESSION_NOT_FOUND"));

        if (session.getStatus() == TvAuthSessionStatus.APPROVED) {
            session.setStatus(TvAuthSessionStatus.USED);
            sessionRepository.save(session);
            log.info("Marked TV auth session as used: {}", sessionId);
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredSessions() {
        int expired = sessionRepository.expireOldSessions(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} old TV auth sessions", expired);
        }
    }

    private TvAuthSessionEntity findSession(String sessionIdOrCode) {
        // Try by session ID first (longer string)
        if (sessionIdOrCode.length() > 10) {
            return sessionRepository.findBySessionId(sessionIdOrCode)
                    .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Session not found", "SESSION_NOT_FOUND"));
        }
        // Try by session code
        return sessionRepository.findBySessionCode(sessionIdOrCode.toUpperCase())
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Session not found", "SESSION_NOT_FOUND"));
    }

    private String registerOrUpdateDevice(TvAuthSessionEntity session, OrganizationEntity organization) {
        // Check if device already registered
        return deviceRepository.findByDeviceIdAndOrganization_Id(session.getDeviceId(), organization.getId())
                .map(existing -> {
                    // Reactivate if needed
                    if (!existing.getActive()) {
                        existing.setActive(true);
                        existing.setTrustScore(100);
                        existing.setIsTrusted(true);
                        deviceRepository.save(existing);
                        log.info("Reactivated existing TV device: {}", session.getDeviceId());
                    }
                    return existing.getDeviceToken();
                })
                .orElseGet(() -> {
                    // Register new device via service
                    TvDeviceRegistrationRequestDTO regRequest = TvDeviceRegistrationRequestDTO.builder()
                            .deviceId(session.getDeviceId())
                            .organizationId(organization.getId())
                            .publicKey("qr-auth-" + UUID.randomUUID()) // Placeholder, can be updated later
                            .deviceName("TV-" + session.getSessionCode())
                            .build();

                    TvDeviceRegistrationResponseDTO response = deviceRegistrationService.registerDevice(regRequest);
                    log.info("Registered new TV device via QR auth: {}", session.getDeviceId());
                    return response.getDeviceToken();
                });
    }

    private String generateSessionCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        code.append("-");
        for (int i = 0; i < 3; i++) {
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }

        // Ensure uniqueness
        String finalCode = code.toString();
        if (sessionRepository.findBySessionCode(finalCode).isPresent()) {
            return generateSessionCode(); // Retry
        }
        return finalCode;
    }

    private TvAuthSessionDTO mapToDTO(TvAuthSessionEntity session) {
        TvAuthSessionDTO.TvAuthSessionDTOBuilder builder = TvAuthSessionDTO.builder()
                .sessionId(session.getSessionId())
                .sessionCode(session.getSessionCode())
                .status(session.getStatus())
                .qrCodeUrl(frontendUrl + "/tv-auth?code=" + session.getSessionId())
                .expiresAt(session.getExpiresAt())
                .expiresInSeconds(ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getExpiresAt()));

        // Include tokens only if approved
        if (session.getStatus() == TvAuthSessionStatus.APPROVED || session.getStatus() == TvAuthSessionStatus.USED) {
            builder.accessToken(session.getAccessToken())
                    .deviceToken(session.getAccessToken()); // Same as access token for TV

            if (session.getOrganization() != null) {
                builder.organizationId(session.getOrganization().getId())
                        .organizationName(session.getOrganization().getName());
            }
        }

        return builder.build();
    }
}
