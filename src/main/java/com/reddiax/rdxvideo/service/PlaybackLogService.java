package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.PlaybackLogBatchRequest;
import com.reddiax.rdxvideo.model.dto.PlaybackLogBatchResponse;
import com.reddiax.rdxvideo.model.dto.PlaybackLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaybackLogService {

    PlaybackLogBatchResponse saveBatch(String deviceToken, PlaybackLogBatchRequest request);

    Page<PlaybackLogDTO> getByOrganization(Long organizationId, Pageable pageable);

    Page<PlaybackLogDTO> getByDevice(Long deviceId, Pageable pageable);
}
