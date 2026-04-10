package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PricingTierDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
