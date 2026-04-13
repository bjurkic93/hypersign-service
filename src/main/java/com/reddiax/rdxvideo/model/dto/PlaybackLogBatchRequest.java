package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackLogBatchRequest {

    @NotEmpty(message = "Logs list cannot be empty")
    @Valid
    private List<PlaybackLogEntry> logs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaybackLogEntry {
        @NotNull(message = "Content ID is required")
        private Long contentId;

        @NotNull(message = "Content type is required")
        private String contentType;

        @NotNull(message = "Started at timestamp is required")
        private LocalDateTime startedAt;

        @NotNull(message = "Duration is required")
        private Integer durationMs;
    }
}
