package com.reddiax.rdxvideo.model.dto;

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
public class LayoutDTO {
    private Long id;
    private String name;
    private String description;
    private Integer width;
    private Integer height;
    private String orientation;
    private List<LayoutSectionDTO> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
