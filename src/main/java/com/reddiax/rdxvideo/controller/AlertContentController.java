package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.AlertContentDTO;
import com.reddiax.rdxvideo.model.dto.AlertContentRequest;
import com.reddiax.rdxvideo.service.AlertContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/content/alert")
@Tag(name = "Alert Content", description = "API for managing alert/warning content")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AlertContentController {

    private final AlertContentService service;

    @GetMapping
    @Operation(summary = "Get all alert content")
    public List<AlertContentDTO> getAll(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all alert content", jwt.getSubject());
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert content by ID")
    public AlertContentDTO getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching alert content: {}", jwt.getSubject(), id);
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create alert content")
    public AlertContentDTO create(@Valid @RequestBody AlertContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating alert content: {}", jwt.getSubject(), request.getName());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update alert content")
    public AlertContentDTO update(@PathVariable Long id, @Valid @RequestBody AlertContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating alert content: {}", jwt.getSubject(), id);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete alert content")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting alert content: {}", jwt.getSubject(), id);
        service.delete(id);
    }
}
