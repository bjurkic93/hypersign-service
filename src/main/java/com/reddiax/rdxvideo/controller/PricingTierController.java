package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.PricingTierDTO;
import com.reddiax.rdxvideo.model.dto.PricingTierRequestDTO;
import com.reddiax.rdxvideo.service.PricingTierService;
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
@RequestMapping("api/v1/pricing-tiers")
@Tag(name = "Pricing Tier Management", description = "API for managing TV advertisement pricing tiers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PricingTierController {

    private final PricingTierService pricingTierService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all pricing tiers", description = "Get a paginated list of all pricing tiers")
    public ResponseEntity<Page<PricingTierDTO>> getPricingTiers(
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting pricing tiers", jwt.getSubject());
        return ResponseEntity.ok(pricingTierService.getPricingTiers(pageable));
    }

    @GetMapping(path = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get active pricing tiers", description = "Get a list of all active pricing tiers")
    public ResponseEntity<List<PricingTierDTO>> getActivePricingTiers(
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting active pricing tiers", jwt.getSubject());
        return ResponseEntity.ok(pricingTierService.getActivePricingTiers());
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get pricing tier by ID", description = "Get a pricing tier by its ID")
    public ResponseEntity<PricingTierDTO> getPricingTier(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} getting pricing tier: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(pricingTierService.getPricingTier(id));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create pricing tier", description = "Create a new pricing tier")
    public ResponseEntity<PricingTierDTO> createPricingTier(
            @Valid @RequestBody PricingTierRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating pricing tier: {}", jwt.getSubject(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pricingTierService.createPricingTier(request));
    }

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update pricing tier", description = "Update an existing pricing tier")
    public ResponseEntity<PricingTierDTO> updatePricingTier(
            @PathVariable Long id,
            @Valid @RequestBody PricingTierRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating pricing tier: {}", jwt.getSubject(), id);
        return ResponseEntity.ok(pricingTierService.updatePricingTier(id, request));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete pricing tier", description = "Delete a pricing tier by ID")
    public ResponseEntity<Void> deletePricingTier(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting pricing tier: {}", jwt.getSubject(), id);
        pricingTierService.deletePricingTier(id);
        return ResponseEntity.noContent().build();
    }
}
