package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.constant.AdvertisementPlatformEnum;
import com.reddiax.rdxvideo.model.dto.AdvertisementDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementRequestDTO;
import com.reddiax.rdxvideo.service.AdvertisementService;
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

@Slf4j
@RestController
@RequestMapping("api/v1/advertisements")
@Tag(name = "Advertisement Management", description = "API for managing advertisements")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all advertisements", description = "Get a paginated list of all advertisements")
    public ResponseEntity<Page<AdvertisementDTO>> getAdvertisements(
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting advertisements", jwt.getSubject());
        return ResponseEntity.ok(advertisementService.getAdvertisements(pageable));
    }

    @GetMapping(path = "/organization/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get advertisements by organization", description = "Get advertisements for a specific organization")
    public ResponseEntity<Page<AdvertisementDTO>> getAdvertisementsByOrganization(
            @PathVariable Long organizationId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting advertisements for organization: {}", jwt.getSubject(), organizationId);
        return ResponseEntity.ok(advertisementService.getAdvertisementsByOrganization(organizationId, pageable));
    }

    @GetMapping(path = "/platform/{platform}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get advertisements by platform", description = "Get advertisements for a specific platform (TV or MOBILE)")
    public ResponseEntity<Page<AdvertisementDTO>> getAdvertisementsByPlatform(
            @PathVariable AdvertisementPlatformEnum platform,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting advertisements for platform: {}", jwt.getSubject(), platform);
        return ResponseEntity.ok(advertisementService.getAdvertisementsByPlatform(platform, pageable));
    }

    @GetMapping(path = "/valid", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get valid advertisements", description = "Get currently valid advertisements for a platform and organization")
    public ResponseEntity<Page<AdvertisementDTO>> getValidAdvertisements(
            @RequestParam AdvertisementPlatformEnum platform,
            @RequestParam Long organizationId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting valid advertisements for platform {} and organization {}", 
                jwt.getSubject(), platform, organizationId);
        return ResponseEntity.ok(advertisementService.getValidAdvertisements(platform, organizationId, pageable));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get advertisement by ID", description = "Get an advertisement by its ID")
    public ResponseEntity<AdvertisementDTO> getAdvertisement(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting advertisement: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(advertisementService.getAdvertisement(id));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create advertisement", description = "Create a new advertisement")
    public ResponseEntity<AdvertisementDTO> createAdvertisement(
            @Valid @RequestBody AdvertisementRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating advertisement: {}", jwt.getSubject(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(advertisementService.createAdvertisement(request));
    }

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update advertisement", description = "Update an existing advertisement")
    public ResponseEntity<AdvertisementDTO> updateAdvertisement(
            @PathVariable Long id,
            @Valid @RequestBody AdvertisementRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating advertisement: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(advertisementService.updateAdvertisement(id, request));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete advertisement", description = "Delete an advertisement by ID")
    public ResponseEntity<Void> deleteAdvertisement(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting advertisement: {}", jwt.getSubject(), id);
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }
}
