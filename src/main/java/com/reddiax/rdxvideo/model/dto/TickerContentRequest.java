package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickerContentRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Text is required")
    private String text;

    private Integer speed;
    private String direction;
    private String backgroundColor;
    private String textColor;
    private String fontFamily;
    private Integer fontSize;
    private List<String> tags;
}
