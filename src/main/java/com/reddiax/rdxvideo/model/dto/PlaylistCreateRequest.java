package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private ContentStatusEnum status;
    private Long layoutId;
    private List<PlaylistItemRequest> items;
}
