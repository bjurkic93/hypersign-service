package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.UserDTO;
import com.reddiax.rdxvideo.model.dto.UserProfileUpdateDTO;
import com.reddiax.rdxvideo.model.dto.UserRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface UserService {

    Page<UserDTO> getUsers(Pageable pageable);

    UserDTO getUser(Long id);

    UserDTO getUserByExternalId(String externalId);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getUsersByOrganization(Long organizationId);

    UserDTO createUser(UserRequestDTO request);

    UserDTO updateUser(Long id, UserRequestDTO request);

    void deleteUser(Long id);

    void updateLastLogin(Long id);

    /**
     * JIT (Just-In-Time) provisioning: Find existing user or create new one from JWT claims.
     * This is called on every authenticated request to ensure the user exists in rdx-video.
     *
     * @param jwt The validated JWT token containing user claims
     * @return The user DTO (existing or newly created)
     */
    UserDTO findOrCreateFromJwt(Jwt jwt);

    /**
     * Update the current user's profile (display name, bio, profile picture).
     *
     * @param externalId The user's external ID (JWT subject)
     * @param profileUpdate The profile update data
     * @return The updated user DTO
     */
    UserDTO updateCurrentUserProfile(String externalId, UserProfileUpdateDTO profileUpdate);

    /**
     * Add a user to an organization by email.
     *
     * @param organizationId The organization ID
     * @param email The user's email
     * @return The updated user DTO
     */
    UserDTO addUserToOrganization(Long organizationId, String email);

    /**
     * Remove a user from an organization.
     *
     * @param organizationId The organization ID
     * @param userId The user ID
     */
    void removeUserFromOrganization(Long organizationId, Long userId);
}
