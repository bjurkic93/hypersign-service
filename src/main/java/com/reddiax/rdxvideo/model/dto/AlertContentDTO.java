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
public class AlertContentDTO {
    private Long id;
    private String name;
    private String title;
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
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
