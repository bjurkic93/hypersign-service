package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertisementImageRequestDTO {
    @NotNull(message = "Image ID is required")
    private Long imageId;

    @NotNull(message = "Display order is required")
    @PositiveOrZero(message = "Display order must be zero or positive")
    private Integer displayOrder;

    @Positive(message = "Display duration must be positive")
    private Integer displayDurationSeconds;

    @Positive(message = "Display duration days must be positive")
    private Integer displayDurationDays;
}
