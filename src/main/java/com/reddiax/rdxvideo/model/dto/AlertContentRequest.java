package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class AlertContentRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;

    private String severity;
    private String displayMode;
    private String backgroundColor;
    private String textColor;
    private String iconName;
    private Boolean showIcon;
    private Boolean autoScroll;
    private Integer scrollSpeed;
    private Boolean soundEnabled;
    private String soundUrl;
    private Boolean blinkEnabled;
    private LocalDateTime activeFrom;
    private LocalDateTime activeUntil;
    private List<String> tags;
}
