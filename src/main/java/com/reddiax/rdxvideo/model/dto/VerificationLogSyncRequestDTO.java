package com.reddiax.rdxvideo.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for syncing verification logs from mobile app to server.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationLogSyncRequestDTO {
    
    @NotEmpty(message = "Logs list cannot be empty")
    @Size(max = 100, message = "Maximum 100 logs per sync request")
    @Valid
    private List<VerificationLogItemDTO> logs;
    
    private String deviceToken; // Optional device token for linking to device registration
}
