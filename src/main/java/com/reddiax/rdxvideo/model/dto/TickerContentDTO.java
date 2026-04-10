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
public class TickerContentDTO {
    private Long id;
    private String name;
    private String text;
    private Integer speed;
    private String direction;
    private String backgroundColor;
    private String textColor;
    private String fontFamily;
    private Integer fontSize;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
