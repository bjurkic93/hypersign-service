package com.reddiax.rdxvideo.model.mapper;

import com.reddiax.rdxvideo.model.dto.AnnouncementDTO;
import com.reddiax.rdxvideo.model.entity.AnnouncementEntity;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementMapper {

    public AnnouncementDTO toDTO(AnnouncementEntity entity) {
        if (entity == null) {
            return null;
        }

        return AnnouncementDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .htmlContent(entity.getHtmlContent())
                .channel(entity.getChannel())
                .status(entity.getStatus())
                .scheduledAt(entity.getScheduledAt())
                .sentAt(entity.getSentAt())
                .totalRecipients(entity.getTotalRecipients())
                .successfulDeliveries(entity.getSuccessfulDeliveries())
                .failedDeliveries(entity.getFailedDeliveries())
                .createdByName(entity.getCreatedBy() != null ? entity.getCreatedBy().getDisplayName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
