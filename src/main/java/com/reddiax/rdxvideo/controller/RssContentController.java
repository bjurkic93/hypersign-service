package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.RssContentDTO;
import com.reddiax.rdxvideo.model.dto.RssContentRequest;
import com.reddiax.rdxvideo.model.dto.RssItemDTO;
import com.reddiax.rdxvideo.service.RssContentService;
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
@RequestMapping("api/v1/content/rss")
@Tag(name = "RSS Content", description = "API for managing RSS feed content")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class RssContentController {

    private final RssContentService service;

    @GetMapping
    @Operation(summary = "Get all RSS content")
    public List<RssContentDTO> getAll(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all RSS content", jwt.getSubject());
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get RSS content by ID")
    public RssContentDTO getById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching RSS content: {}", jwt.getSubject(), id);
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create RSS content")
    public RssContentDTO create(@Valid @RequestBody RssContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating RSS content: {}", jwt.getSubject(), request.getName());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update RSS content")
    public RssContentDTO update(@PathVariable Long id, @Valid @RequestBody RssContentRequest request, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating RSS content: {}", jwt.getSubject(), id);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete RSS content")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting RSS content: {}", jwt.getSubject(), id);
        service.delete(id);
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Fetch RSS feed items")
    public List<RssItemDTO> fetchItems(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching RSS items for: {}", jwt.getSubject(), id);
        return service.fetchRssItems(id);
    }

    @GetMapping("/preview")
    @Operation(summary = "Preview RSS feed from URL")
    public List<RssItemDTO> previewFeed(
            @RequestParam String feedUrl,
            @RequestParam(defaultValue = "5") int maxItems,
            @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} previewing RSS feed: {}", jwt.getSubject(), feedUrl);
        return service.previewRssFeed(feedUrl, maxItems);
    }
}
