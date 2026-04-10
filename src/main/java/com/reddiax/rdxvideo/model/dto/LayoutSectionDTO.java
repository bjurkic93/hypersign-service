package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutSectionDTO {
    private String id;
    private String name;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private String contentType;
    private String color;
    private Integer zIndex;
}
