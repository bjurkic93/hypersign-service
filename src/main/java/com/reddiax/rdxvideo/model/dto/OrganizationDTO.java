package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.OrganizationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private OrganizationTypeEnum organizationType;
    private Long logoImageId;
    private String logoUrl;
    private Boolean active;
    private String subscriptionTier;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
