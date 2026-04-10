package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.AddUserToOrganizationRequestDTO;
import com.reddiax.rdxvideo.model.dto.OrganizationDTO;
import com.reddiax.rdxvideo.model.dto.OrganizationRequestDTO;
import com.reddiax.rdxvideo.model.dto.UserDTO;
import com.reddiax.rdxvideo.service.OrganizationService;
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
@RequestMapping("api/v1/organizations")
@Tag(name = "Organization Management", description = "API for managing organizations")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all organizations", description = "Get a paginated list of all organizations")
    public ResponseEntity<Page<OrganizationDTO>> getOrganizations(
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting organizations", jwt.getSubject());
        return ResponseEntity.ok(organizationService.getOrganizations(pageable));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get organization by ID", description = "Get an organization by its ID")
    public ResponseEntity<OrganizationDTO> getOrganization(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting organization: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(organizationService.getOrganization(id));
    }

    @GetMapping(path = "/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get organization by code", description = "Get an organization by its unique code")
    public ResponseEntity<OrganizationDTO> getOrganizationByCode(
            @PathVariable String code,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting organization by code: {}", jwt.getSubject(), code);
        return ResponseEntity.ok(organizationService.getOrganizationByCode(code));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create organization", description = "Create a new organization")
    public ResponseEntity<OrganizationDTO> createOrganization(
            @Valid @RequestBody OrganizationRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating organization: {}", jwt.getSubject(), request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request));
    }

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update organization", description = "Update an existing organization")
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating organization: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(organizationService.updateOrganization(id, request));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete organization", description = "Delete an organization by ID")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting organization: {}", jwt.getSubject(), id);
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    // Organization Users endpoints

    @GetMapping(path = "/{id}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get organization users", description = "Get all users belonging to an organization")
    public ResponseEntity<List<UserDTO>> getOrganizationUsers(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting users for organization: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(userService.getUsersByOrganization(id));
    }

    @PostMapping(path = "/{id}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add user to organization", description = "Add a user to an organization by email")
    public ResponseEntity<UserDTO> addUserToOrganization(
            @PathVariable Long id,
            @Valid @RequestBody AddUserToOrganizationRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} adding user {} to organization: {}", jwt.getSubject(), request.getEmail(), id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.addUserToOrganization(id, request.getEmail()));
    }

    @DeleteMapping(path = "/{id}/users/{userId}")
    @Operation(summary = "Remove user from organization", description = "Remove a user from an organization")
    public ResponseEntity<Void> removeUserFromOrganization(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} removing user {} from organization: {}", jwt.getSubject(), userId, id);
        userService.removeUserFromOrganization(id, userId);
        return ResponseEntity.noContent().build();
    }
}
