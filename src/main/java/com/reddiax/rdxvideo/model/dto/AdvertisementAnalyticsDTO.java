package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementAnalyticsDTO {
    private Long advertisementId;
    private String advertisementName;
    
    private Long activeUsers;
    private Long totalUsers;
    private Long completedUsers;
    private Long violatedUsers;
    
    private Double completionRate;
    private Double violationRate;
}
