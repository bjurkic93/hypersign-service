package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.UserRoleEnum;
import com.reddiax.rdxvideo.constant.UserStatusEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.UserDTO;
import com.reddiax.rdxvideo.model.dto.UserProfileUpdateDTO;
import com.reddiax.rdxvideo.model.dto.UserRequestDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.model.mapper.UserMapper;
import com.reddiax.rdxvideo.repository.OrganizationRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.service.MediaService;
import com.reddiax.rdxvideo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MediaService mediaService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toUserDtoWithResolvedUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        return toUserDtoWithResolvedUrl(findUserById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByExternalId(String externalId) {
        return toUserDtoWithResolvedUrl(
                userRepository.findByExternalId(externalId)
                        .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                                "User not found with externalId: " + externalId, "USER_NOT_FOUND"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        return toUserDtoWithResolvedUrl(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                                "User not found with email: " + email, "USER_NOT_FOUND"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByOrganization(Long organizationId) {
        return userRepository.findByOrganizationId(organizationId).stream()
                .map(this::toUserDtoWithResolvedUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RdXException(HttpStatus.CONFLICT,
                    "User with email already exists: " + request.getEmail(), "USER_EMAIL_EXISTS");
        }

        if (userRepository.existsByExternalId(request.getExternalId())) {
            throw new RdXException(HttpStatus.CONFLICT,
                    "User with externalId already exists: " + request.getExternalId(), "USER_EXTERNAL_ID_EXISTS");
        }

        UserEntity entity = UserMapper.INSTANCE.toCreateEntity(request);

        if (request.getOrganizationId() != null) {
            OrganizationEntity organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                            "Organization not found with id: " + request.getOrganizationId(), "ORGANIZATION_NOT_FOUND"));
            entity.setOrganization(organization);
        }

        return toUserDtoWithResolvedUrl(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserRequestDTO request) {
        UserEntity existingEntity = findUserById(id);

        // Check if email is being changed and if new email already exists
        if (!existingEntity.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RdXException(HttpStatus.CONFLICT,
                    "User with email already exists: " + request.getEmail(), "USER_EMAIL_EXISTS");
        }

        UserMapper.INSTANCE.updateEntity(request, existingEntity);

        if (request.getOrganizationId() != null) {
            OrganizationEntity organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                            "Organization not found with id: " + request.getOrganizationId(), "ORGANIZATION_NOT_FOUND"));
            existingEntity.setOrganization(organization);
        } else {
            existingEntity.setOrganization(null);
        }

        return toUserDtoWithResolvedUrl(userRepository.save(existingEntity));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RdXException(HttpStatus.NOT_FOUND,
                    "User not found with id: " + id, "USER_NOT_FOUND");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long id) {
        UserEntity entity = findUserById(id);
        entity.setLastLoginAt(LocalDateTime.now());
        userRepository.save(entity);
    }

    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "User not found with id: " + id, "USER_NOT_FOUND"));
    }

    /**
     * Convert UserEntity to UserDTO with resolved profile picture URL and organization logo URL.
     */
    private UserDTO toUserDtoWithResolvedUrl(UserEntity entity) {
        UserDTO dto = UserMapper.INSTANCE.toDto(entity);
        
        // Resolve profile picture URL from imageId
        if (entity.getProfileImageId() != null) {
            try {
                String url = mediaService.getMediaUrl(entity.getProfileImageId()).getUrl();
                dto.setProfilePictureUrl(url);
            } catch (Exception e) {
                log.warn("Failed to resolve profile image URL for user {}: {}", entity.getId(), e.getMessage());
                dto.setProfilePictureUrl(null);
            }
        }
        
        // Resolve organization logo URL from organization's logoImageId
        if (entity.getOrganization() != null) {
            OrganizationEntity org = entity.getOrganization();
            if (org.getLogoImageId() != null) {
                try {
                    String logoUrl = mediaService.getMediaUrl(org.getLogoImageId()).getUrl();
                    dto.setOrganizationLogoUrl(logoUrl);
                } catch (Exception e) {
                    log.warn("Failed to resolve organization logo URL for org {}: {}", org.getId(), e.getMessage());
                    // Try fallback to stored logoUrl
                    dto.setOrganizationLogoUrl(org.getLogoUrl());
                }
            } else if (org.getLogoUrl() != null) {
                dto.setOrganizationLogoUrl(org.getLogoUrl());
            }
        }
        
        return dto;
    }

    /**
     * JIT (Just-In-Time) provisioning implementation.
     * Extracts user info from JWT and creates user if not exists.
     * For MOBILE_PUBLISHER users, automatically creates a wallet.
     */
    @Override
    @Transactional
    public UserDTO findOrCreateFromJwt(Jwt jwt) {
        String externalId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        // Try to find existing user
        Optional<UserEntity> existingUser = userRepository.findByExternalId(externalId);

        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            // Sync email if changed in auth server
            if (email != null && !email.equals(user.getEmail())) {
                if (!userRepository.existsByEmail(email)) {
                    log.info("JIT: Syncing email for user {} from {} to {}", externalId, user.getEmail(), email);
                    user.setEmail(email);
                }
            }
            // Sync role from JWT (e.g. user granted SYSTEM_ADMIN in auth server)
            UserRoleEnum jwtRole = mapRoleFromJwt(jwt);
            if (jwtRole != user.getRole()) {
                log.info("JIT: Syncing role for user {} from {} to {}", externalId, user.getRole(), jwtRole);
                user.setRole(jwtRole);
            }
            return toUserDtoWithResolvedUrl(userRepository.save(user));
        }

        // Create new user via JIT provisioning
        UserRoleEnum role = mapRoleFromJwt(jwt);
        log.info("JIT: Creating new user with externalId: {}, email: {}, role: {}", externalId, email, role);

        UserEntity newUser = UserEntity.builder()
                .externalId(externalId)
                .email(email)
                .displayName(buildDisplayName(jwt))
                .role(role)
                .status(UserStatusEnum.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .organization(null) // MOBILE_PUBLISHER and TV_PUBLISHER don't belong to any organization
                .build();

        UserEntity savedUser = userRepository.save(newUser);
        log.info("JIT: Successfully created user with id: {}, externalId: {}, role: {}", savedUser.getId(), externalId, role);

        return toUserDtoWithResolvedUrl(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateCurrentUserProfile(String externalId, UserProfileUpdateDTO profileUpdate) {
        UserEntity user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "User not found with externalId: " + externalId, "USER_NOT_FOUND"));

        if (profileUpdate.getDisplayName() != null) {
            user.setDisplayName(profileUpdate.getDisplayName());
        }
        if (profileUpdate.getBio() != null) {
            user.setBio(profileUpdate.getBio());
        }
        if (profileUpdate.getProfileImageId() != null) {
            // Setting to 0 or negative removes the profile image
            if (profileUpdate.getProfileImageId() <= 0) {
                user.setProfileImageId(null);
            } else {
                user.setProfileImageId(profileUpdate.getProfileImageId());
            }
        }
        
        if (profileUpdate.getOrganizationId() != null) {
            // Setting to 0 or negative removes the organization
            if (profileUpdate.getOrganizationId() <= 0) {
                user.setOrganization(null);
            } else {
                OrganizationEntity organization = organizationRepository.findById(profileUpdate.getOrganizationId())
                        .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                                "Organization not found with id: " + profileUpdate.getOrganizationId(), "ORGANIZATION_NOT_FOUND"));
                user.setOrganization(organization);
            }
        }

        return toUserDtoWithResolvedUrl(userRepository.save(user));
    }

    /**
     * Build display name from JWT claims (givenName + familyName or email prefix)
     */
    private String buildDisplayName(Jwt jwt) {
        String givenName = jwt.getClaimAsString("givenName");
        String familyName = jwt.getClaimAsString("familyName");

        if (givenName != null && familyName != null) {
            return givenName + " " + familyName;
        } else if (givenName != null) {
            return givenName;
        }

        // Fallback to email prefix
        String email = jwt.getClaimAsString("email");
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }

        return "User";
    }

    // OAuth2 Client IDs
    private static final String CLIENT_ID_CMS = "rdx-video-cms";
    private static final String CLIENT_ID_VIDEO_ANDROID = "rdx-video-android";
    private static final String CLIENT_ID_GLANCE_ANDROID = "rdx-glance-android";
    
    /**
     * Map role from JWT claims. Checks for specific roles and OAuth2 client_id.
     */
    private UserRoleEnum mapRoleFromJwt(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        
        // Check for admin/moderator roles first (from auth server)
        if (roles != null) {
            if (roles.contains("ROLE_SYSTEM_ADMIN") || roles.contains("SYSTEM_ADMIN")) {
                return UserRoleEnum.SYSTEM_ADMIN;
            }
            if (roles.contains("ROLE_ADMIN") || roles.contains("ADMIN")) {
                return UserRoleEnum.ADMIN;
            }
            if (roles.contains("ROLE_MODERATOR") || roles.contains("MODERATOR")) {
                return UserRoleEnum.MODERATOR;
            }
        }
        
        // Check client_id to determine platform
        // "azp" (authorized party) is standard OAuth2 claim for client_id
        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null) {
            clientId = jwt.getClaimAsString("client_id");
        }
        
        log.debug("Mapping role for client_id: {}", clientId);
        
        if (clientId != null) {
            switch (clientId) {
                case CLIENT_ID_GLANCE_ANDROID:
                    // RdX Glance - mobile publisher app (earns rewards on lock screen)
                    return UserRoleEnum.MOBILE_PUBLISHER;
                    
                case CLIENT_ID_VIDEO_ANDROID:
                    // RdX Video Player - TV publisher app (earns rewards watching videos)
                    return UserRoleEnum.TV_PUBLISHER;
                    
                case CLIENT_ID_CMS:
                    // Web CMS - standard user (needs organization)
                    return UserRoleEnum.USER;
            }
        }
        
        // Default to USER for unknown clients
        return UserRoleEnum.USER;
    }

    @Override
    @Transactional
    public UserDTO addUserToOrganization(Long organizationId, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "User not found with email: " + email, "USER_NOT_FOUND"));

        OrganizationEntity organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND,
                        "Organization not found with id: " + organizationId, "ORGANIZATION_NOT_FOUND"));

        user.setOrganization(organization);
        return toUserDtoWithResolvedUrl(userRepository.save(user));
    }

    @Override
    @Transactional
    public void removeUserFromOrganization(Long organizationId, Long userId) {
        UserEntity user = findUserById(userId);

        if (user.getOrganization() == null || !user.getOrganization().getId().equals(organizationId)) {
            throw new RdXException(HttpStatus.BAD_REQUEST,
                    "User does not belong to this organization", "USER_NOT_IN_ORGANIZATION");
        }

        user.setOrganization(null);
        userRepository.save(user);
    }
}
