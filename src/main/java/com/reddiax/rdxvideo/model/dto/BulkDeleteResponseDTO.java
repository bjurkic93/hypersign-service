package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteResponseDTO {
    private int totalRequested;
    private int successCount;
    private int failedCount;
    private List<BulkDeleteError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkDeleteError {
        private Long id;
        private String filename;
        private String reason;
    }
}
