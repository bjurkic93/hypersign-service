package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackLogDTO {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private Long organizationId;
    private Long contentId;
    private String contentType;
    private LocalDateTime startedAt;
    private Integer durationMs;
    private LocalDateTime receivedAt;
}
