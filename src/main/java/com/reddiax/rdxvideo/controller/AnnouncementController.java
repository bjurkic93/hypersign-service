package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.AnnouncementDTO;
import com.reddiax.rdxvideo.model.dto.CreateAnnouncementRequestDTO;
import com.reddiax.rdxvideo.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing announcements.
 * Only accessible by SYSTEM_ADMIN (global administrators).
 */
@RestController
@RequestMapping("/api/v1/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@Tag(name = "Announcements", description = "System admin API for managing announcements to all users")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @Operation(summary = "Create a new announcement")
    public ResponseEntity<AnnouncementDTO> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequestDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String creatorId = jwt.getSubject();
        AnnouncementDTO announcement = announcementService.createAnnouncement(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(announcement);
    }

    @GetMapping
    @Operation(summary = "Get all announcements with pagination")
    public ResponseEntity<Page<AnnouncementDTO>> getAllAnnouncements(Pageable pageable) {
        return ResponseEntity.ok(announcementService.getAllAnnouncements(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID")
    public ResponseEntity<AnnouncementDTO> getAnnouncementById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send a draft announcement immediately")
    public ResponseEntity<AnnouncementDTO> sendAnnouncementNow(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.sendAnnouncementNow(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a scheduled or draft announcement")
    public ResponseEntity<AnnouncementDTO> cancelAnnouncement(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.cancelAnnouncement(id));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get announcement statistics")
    public ResponseEntity<AnnouncementService.AnnouncementStatsDTO> getStats() {
        return ResponseEntity.ok(announcementService.getStats());
    }
}
