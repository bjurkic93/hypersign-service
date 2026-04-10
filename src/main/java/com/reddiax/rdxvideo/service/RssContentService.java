package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.RssContentDTO;
import com.reddiax.rdxvideo.model.dto.RssContentRequest;
import com.reddiax.rdxvideo.model.dto.RssItemDTO;

import java.util.List;

public interface RssContentService {
    List<RssContentDTO> getAll();
    RssContentDTO getById(Long id);
    RssContentDTO create(RssContentRequest request);
    RssContentDTO update(Long id, RssContentRequest request);
    void delete(Long id);
    List<RssItemDTO> fetchRssItems(Long id);
    List<RssItemDTO> previewRssFeed(String feedUrl, int maxItems);
}
