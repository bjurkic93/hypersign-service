package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaPageResponseDTO {
    private List<MediaListItemDTO> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
