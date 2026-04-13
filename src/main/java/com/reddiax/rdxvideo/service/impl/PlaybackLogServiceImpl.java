package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.PlaybackLogBatchRequest;
import com.reddiax.rdxvideo.model.dto.PlaybackLogBatchResponse;
import com.reddiax.rdxvideo.model.dto.PlaybackLogDTO;
import com.reddiax.rdxvideo.model.entity.PlaybackLogEntity;
import com.reddiax.rdxvideo.model.entity.TvDeviceRegistrationEntity;
import com.reddiax.rdxvideo.repository.PlaybackLogRepository;
import com.reddiax.rdxvideo.repository.TvDeviceRegistrationRepository;
import com.reddiax.rdxvideo.service.PlaybackLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaybackLogServiceImpl implements PlaybackLogService {

    private final PlaybackLogRepository playbackLogRepository;
    private final TvDeviceRegistrationRepository deviceRepository;

    @Override
    @Transactional
    public PlaybackLogBatchResponse saveBatch(String deviceToken, PlaybackLogBatchRequest request) {
        TvDeviceRegistrationEntity device = deviceRepository.findByDeviceToken(deviceToken)
                .orElseThrow(() -> new RdXException(HttpStatus.UNAUTHORIZED, "Invalid device token", "INVALID_DEVICE_TOKEN"));

        if (!device.getActive()) {
            throw new RdXException(HttpStatus.FORBIDDEN, "Device is not active", "DEVICE_INACTIVE");
        }

        Long organizationId = device.getOrganization().getId();
        List<PlaybackLogEntity> entities = new ArrayList<>();

        for (PlaybackLogBatchRequest.PlaybackLogEntry entry : request.getLogs()) {
            PlaybackLogEntity entity = PlaybackLogEntity.builder()
                    .device(device)
                    .organizationId(organizationId)
                    .contentId(entry.getContentId())
                    .contentType(entry.getContentType())
                    .startedAt(entry.getStartedAt())
                    .durationMs(entry.getDurationMs())
                    .build();
            entities.add(entity);
        }

        playbackLogRepository.saveAll(entities);
        log.info("Saved {} playback logs for device {} (org {})", entities.size(), device.getId(), organizationId);

        return PlaybackLogBatchResponse.builder()
                .received(entities.size())
                .message("OK")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlaybackLogDTO> getByOrganization(Long organizationId, Pageable pageable) {
        return playbackLogRepository.findByOrganizationIdOrderByStartedAtDesc(organizationId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlaybackLogDTO> getByDevice(Long deviceId, Pageable pageable) {
        return playbackLogRepository.findByDeviceIdOrderByStartedAtDesc(deviceId, pageable)
                .map(this::mapToDTO);
    }

    private PlaybackLogDTO mapToDTO(PlaybackLogEntity entity) {
        return PlaybackLogDTO.builder()
                .id(entity.getId())
                .deviceId(entity.getDevice().getId())
                .deviceName(entity.getDevice().getDeviceName())
                .organizationId(entity.getOrganizationId())
                .contentId(entity.getContentId())
                .contentType(entity.getContentType())
                .startedAt(entity.getStartedAt())
                .durationMs(entity.getDurationMs())
                .receivedAt(entity.getReceivedAt())
                .build();
    }
}
