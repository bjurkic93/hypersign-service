package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RssContentRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Feed URL is required")
    @URL(message = "Feed URL must be a valid URL")
    private String feedUrl;

    private Integer refreshIntervalMinutes;
    private Integer maxItems;
    private Boolean showImages;
    private Boolean showDescription;
    private String displayMode;
    private String backgroundColor;
    private String textColor;
    private String fontFamily;
    private Integer fontSize;
    private List<String> tags;
}
