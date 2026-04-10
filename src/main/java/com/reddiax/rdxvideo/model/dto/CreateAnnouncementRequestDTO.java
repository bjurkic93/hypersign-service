package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.AnnouncementChannelEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAnnouncementRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    /**
     * Optional HTML content for email body.
     * If not provided, the plain message will be used.
     */
    private String htmlContent;

    @NotNull(message = "Channel is required")
    private AnnouncementChannelEnum channel;

    /**
     * If provided, the announcement will be scheduled for this time.
     * If null, the announcement will be sent immediately.
     */
    private LocalDateTime scheduledAt;
}
