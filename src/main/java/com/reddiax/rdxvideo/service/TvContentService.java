package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.TvContentResponse;

public interface TvContentService {

    /**
     * Get the current active content for a TV device.
     * Returns the active schedule with playlist, layout, and content items.
     *
     * @param organizationId the organization ID from device token
     * @return TV content response with all playback data
     */
    TvContentResponse getTvContent(Long organizationId);
}
