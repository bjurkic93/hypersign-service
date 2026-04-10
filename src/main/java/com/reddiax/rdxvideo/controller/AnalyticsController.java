package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.*;
import com.reddiax.rdxvideo.service.TvAnalyticsService;
import com.reddiax.rdxvideo.service.UserAdvertisementService;
import com.reddiax.rdxvideo.service.VerificationAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Analytics API endpoints for CMS.
 * Requires ADMIN or USER role with organization access.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs for CMS dashboard")
public class AnalyticsController {

    private final UserAdvertisementService userAdvertisementService;
    private final VerificationAnalyticsService verificationAnalyticsService;
    private final TvAnalyticsService tvAnalyticsService;

    // ==================== Global Analytics (Admin only) ====================

    @GetMapping("/global")
    @Operation(summary = "Get global analytics", 
               description = "Get platform-wide analytics (admin only)")
    public ResponseEntity<GlobalAnalyticsDTO> getGlobalAnalytics() {
        return ResponseEntity.ok(userAdvertisementService.getGlobalAnalytics());
    }

    // ==================== Organization Analytics ====================

    @GetMapping("/organizations/{organizationId}")
    @Operation(summary = "Get organization analytics", 
               description = "Get analytics for a specific organization")
    public ResponseEntity<GlobalAnalyticsDTO> getOrganizationAnalytics(
            @PathVariable Long organizationId) {
        return ResponseEntity.ok(userAdvertisementService.getOrganizationAnalytics(organizationId));
    }

    // ==================== Advertisement Analytics ====================

    @GetMapping("/advertisements/{advertisementId}")
    @Operation(summary = "Get advertisement analytics", 
               description = "Get detailed analytics for a specific advertisement")
    public ResponseEntity<AdvertisementAnalyticsDTO> getAdvertisementAnalytics(
            @PathVariable Long advertisementId) {
        return ResponseEntity.ok(userAdvertisementService.getAdvertisementAnalytics(advertisementId));
    }

    @GetMapping("/advertisements/{advertisementId}/users")
    @Operation(summary = "Get advertisement users", 
               description = "Get list of users who have shown this advertisement")
    public ResponseEntity<Page<UserAdvertisementDTO>> getAdvertisementUsers(
            @PathVariable Long advertisementId,
            Pageable pageable) {
        return ResponseEntity.ok(userAdvertisementService.getUsersByAdvertisement(advertisementId, pageable));
    }

    // ==================== Verification Log Analytics ====================

    @GetMapping("/advertisements/{advertisementId}/timeline")
    @Operation(summary = "Get advertisement verification timeline", 
               description = "Get time-series verification data for an advertisement")
    public ResponseEntity<List<AdvertisementTimelineDTO>> getAdvertisementTimeline(
            @PathVariable Long advertisementId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "HOUR") String interval) {
        return ResponseEntity.ok(verificationAnalyticsService.getTimeline(advertisementId, startDate, endDate, interval));
    }

    @GetMapping("/advertisements/{advertisementId}/devices")
    @Operation(summary = "Get per-device analytics", 
               description = "Get verification analytics breakdown by device for an advertisement")
    public ResponseEntity<List<AdvertisementDeviceAnalyticsDTO>> getAdvertisementDeviceAnalytics(
            @PathVariable Long advertisementId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(verificationAnalyticsService.getDeviceAnalytics(advertisementId, limit));
    }

    @GetMapping("/advertisements/{advertisementId}/summary")
    @Operation(summary = "Get advertisement summary", 
               description = "Get summary statistics for an advertisement including total display time and success rate")
    public ResponseEntity<AdvertisementSummaryDTO> getAdvertisementSummary(
            @PathVariable Long advertisementId) {
        return ResponseEntity.ok(verificationAnalyticsService.getSummary(advertisementId));
    }

    // ==================== TV Analytics (Glance TV) ====================

    @GetMapping("/tv/summary")
    @Operation(summary = "Get TV analytics summary", 
               description = "Get summary of TV advertisement impressions, earnings, and devices")
    public ResponseEntity<TvAnalyticsSummaryDTO> getTvAnalyticsSummary() {
        return ResponseEntity.ok(tvAnalyticsService.getSummary());
    }

    @GetMapping("/tv/devices")
    @Operation(summary = "Get TV device analytics", 
               description = "Get analytics for registered TV devices")
    public ResponseEntity<List<TvDeviceAnalyticsDTO>> getTvDeviceAnalytics(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tvAnalyticsService.getDeviceAnalytics(limit));
    }

    @GetMapping("/tv/timeline")
    @Operation(summary = "Get TV impression timeline", 
               description = "Get time-series impression data for TV advertisements")
    public ResponseEntity<List<TvImpressionTimelineDTO>> getTvImpressionTimeline(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "HOUR") String interval) {
        return ResponseEntity.ok(tvAnalyticsService.getTimeline(startDate, endDate, interval));
    }

    @GetMapping("/tv/advertisements")
    @Operation(summary = "Get TV advertisement stats", 
               description = "Get performance statistics for TV advertisements")
    public ResponseEntity<List<TvAdvertisementStatsDTO>> getTvAdvertisementStats(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tvAnalyticsService.getAdvertisementStats(limit));
    }
}
