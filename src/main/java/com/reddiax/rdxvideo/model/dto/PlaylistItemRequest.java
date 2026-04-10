package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistItemRequest {
    private Long mediaId;
    
    private String contentType; // MEDIA, TICKER, RSS, WEBPAGE
    
    private Long contentId;
    
    @NotNull
    @PositiveOrZero
    private Integer orderIndex;
    
    @Positive
    private Integer durationSeconds;
    
    private String sectionId;
}
