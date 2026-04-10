package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for advertisement video items.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementVideoRequestDTO {
    
    @NotNull(message = "Video ID is required")
    private Long videoId;

    @NotNull(message = "Display order is required")
    @PositiveOrZero(message = "Display order must be zero or positive")
    private Integer displayOrder;

    /**
     * Display duration in seconds.
     * If null, the full video length is used.
     */
    @Positive(message = "Display duration must be positive")
    private Integer displayDurationSeconds;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
}
