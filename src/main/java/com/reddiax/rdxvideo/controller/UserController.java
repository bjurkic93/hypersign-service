package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.UserDTO;
import com.reddiax.rdxvideo.model.dto.UserProfileUpdateDTO;
import com.reddiax.rdxvideo.model.dto.UserRequestDTO;
import com.reddiax.rdxvideo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/users")
@Tag(name = "User Management", description = "API for managing users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current user",
            description = "Get the current authenticated user's profile. Creates user via JIT provisioning if not exists.")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.debug("Getting current user profile for: {}", jwt.getSubject());
        return ResponseEntity.ok(userService.findOrCreateFromJwt(jwt));
    }

    @PatchMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update current user profile",
            description = "Update the current authenticated user's profile (display name, bio, profile picture)")
    public ResponseEntity<UserDTO> updateCurrentUser(
            @Valid @RequestBody UserProfileUpdateDTO profileUpdate,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Updating profile for user: {}", jwt.getSubject());
        return ResponseEntity.ok(userService.updateCurrentUserProfile(jwt.getSubject(), profileUpdate));
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all users", description = "Get a paginated list of all users")
    public ResponseEntity<Page<UserDTO>> getUsers(
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting users", jwt.getSubject());
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user by ID", description = "Get a user by their ID")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting user: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping(path = "/external/{externalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user by external ID", description = "Get a user by their external ID (OAuth2 subject)")
    public ResponseEntity<UserDTO> getUserByExternalId(
            @PathVariable String externalId,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting user by externalId: {}", jwt.getSubject(), externalId);
        return ResponseEntity.ok(userService.getUserByExternalId(externalId));
    }

    @GetMapping(path = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user by email", description = "Get a user by their email address")
    public ResponseEntity<UserDTO> getUserByEmail(
            @PathVariable String email,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting user by email: {}", jwt.getSubject(), email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping(path = "/organization/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get users by organization", description = "Get all users belonging to an organization")
    public ResponseEntity<List<UserDTO>> getUsersByOrganization(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting users by organization: {}", jwt.getSubject(), organizationId);
        return ResponseEntity.ok(userService.getUsersByOrganization(organizationId));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody UserRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating user: {}", jwt.getSubject(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating user: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by ID")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting user: {}", jwt.getSubject(), id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/login")
    @Operation(summary = "Update last login", description = "Update the last login timestamp for a user")
    public ResponseEntity<Void> updateLastLogin(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("Updating last login for user: {}", id);
        userService.updateLastLogin(id);
        return ResponseEntity.ok().build();
    }
}
