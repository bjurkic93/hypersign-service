package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private ContentStatusEnum status;
    private Long layoutId;
    private LayoutDTO layout;
    private List<PlaylistItemDTO> items;
    private Integer totalDurationSeconds;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
