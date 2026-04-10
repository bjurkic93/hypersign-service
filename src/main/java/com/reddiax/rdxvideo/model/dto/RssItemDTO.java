package com.reddiax.rdxvideo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RssItemDTO {
    private String title;
    private String description;
    private String link;
    private String imageUrl;
    private String pubDate;
}
