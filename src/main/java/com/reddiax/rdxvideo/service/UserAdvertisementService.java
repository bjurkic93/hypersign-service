package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserAdvertisementService {

    // ==================== CMS/Admin Endpoints ====================
    
    /**
     * Get all users for a specific advertisement
     */
    Page<UserAdvertisementDTO> getUsersByAdvertisement(Long advertisementId, Pageable pageable);
    
    /**
     * Get analytics for a specific advertisement
     */
    AdvertisementAnalyticsDTO getAdvertisementAnalytics(Long advertisementId);
    
    /**
     * Get global analytics
     */
    GlobalAnalyticsDTO getGlobalAnalytics();
    
    /**
     * Get analytics for an organization
     */
    GlobalAnalyticsDTO getOrganizationAnalytics(Long organizationId);

    // ==================== Background Jobs ====================
    
    /**
     * Process expired advertisements (called by scheduler)
     */
    void processExpiredAdvertisements();
    
    /**
     * Process stale advertisements that haven't been verified recently
     */
    void processStaleAdvertisements();
}
