package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to approve a TV auth session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TvAuthApproveRequest {
    
    @NotNull(message = "Organization ID is required")
    private Long organizationId;
}
