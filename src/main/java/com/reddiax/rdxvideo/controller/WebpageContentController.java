package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.WebpageContentDTO;
import com.reddiax.rdxvideo.model.dto.WebpageContentRequest;
import com.reddiax.rdxvideo.service.WebpageContentService;
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
@RequestMapping("api/v1/content/webpage")
@Tag(name = "Webpage Content", description = "API for managing embedded webpage content")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WebpageContentController {

    private final WebpageContentService service;

    @GetMapping
    @Operation(summary = "Get all webpage content")
    public List<WebpageContentDTO> getAll(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all webpage content", jwt.getSubject());
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get webpage content by ID")
    public WebpageContentDTO getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching webpage content: {}", jwt.getSubject(), id);
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create webpage content")
    public WebpageContentDTO create(@Valid @RequestBody WebpageContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating webpage content: {}", jwt.getSubject(), request.getName());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update webpage content")
    public WebpageContentDTO update(@PathVariable Long id, @Valid @RequestBody WebpageContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating webpage content: {}", jwt.getSubject(), id);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete webpage content")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting webpage content: {}", jwt.getSubject(), id);
        service.delete(id);
    }
}
