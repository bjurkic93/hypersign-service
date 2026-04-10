package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.AnnouncementDTO;
import com.reddiax.rdxvideo.model.dto.CreateAnnouncementRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnnouncementService {

    /**
     * Create a new announcement.
     * If scheduledAt is null, the announcement will be sent immediately.
     */
    AnnouncementDTO createAnnouncement(CreateAnnouncementRequestDTO request, String creatorExternalId);

    /**
     * Get all announcements with pagination.
     */
    Page<AnnouncementDTO> getAllAnnouncements(Pageable pageable);

    /**
     * Get announcement by ID.
     */
    AnnouncementDTO getAnnouncementById(Long id);

    /**
     * Cancel a scheduled or draft announcement.
     */
    AnnouncementDTO cancelAnnouncement(Long id);

    /**
     * Send a draft announcement immediately.
     */
    AnnouncementDTO sendAnnouncementNow(Long id);

    /**
     * Process scheduled announcements that are ready to be sent.
     * Called by scheduler.
     */
    void processScheduledAnnouncements();

    /**
     * Get statistics about announcements.
     */
    AnnouncementStatsDTO getStats();

    record AnnouncementStatsDTO(
            long totalAnnouncements,
            long draftCount,
            long scheduledCount,
            long sentCount,
            long totalActiveUsers,
            long totalDevicesWithPush
    ) {}
}
