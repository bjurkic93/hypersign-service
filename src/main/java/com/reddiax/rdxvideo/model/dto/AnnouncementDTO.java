package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.AnnouncementChannelEnum;
import com.reddiax.rdxvideo.constant.AnnouncementStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementDTO {

    private Long id;
    private String title;
    private String message;
    private String htmlContent;
    private AnnouncementChannelEnum channel;
    private AnnouncementStatusEnum status;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Integer totalRecipients;
    private Integer successfulDeliveries;
    private Integer failedDeliveries;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
