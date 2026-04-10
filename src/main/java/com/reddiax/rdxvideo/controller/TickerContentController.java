package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.TickerContentDTO;
import com.reddiax.rdxvideo.model.dto.TickerContentRequest;
import com.reddiax.rdxvideo.service.TickerContentService;
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
@RequestMapping("api/v1/content/ticker")
@Tag(name = "Ticker Content", description = "API for managing ticker/marquee text content")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TickerContentController {

    private final TickerContentService service;

    @GetMapping
    @Operation(summary = "Get all ticker content")
    public List<TickerContentDTO> getAll(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all ticker content", jwt.getSubject());
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticker content by ID")
    public TickerContentDTO getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching ticker content: {}", jwt.getSubject(), id);
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create ticker content")
    public TickerContentDTO create(@Valid @RequestBody TickerContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating ticker content: {}", jwt.getSubject(), request.getName());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticker content")
    public TickerContentDTO update(@PathVariable Long id, @Valid @RequestBody TickerContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating ticker content: {}", jwt.getSubject(), id);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete ticker content")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting ticker content: {}", jwt.getSubject(), id);
        service.delete(id);
    }
}
