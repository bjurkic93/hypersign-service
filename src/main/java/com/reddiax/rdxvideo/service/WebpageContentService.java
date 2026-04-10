package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.WebpageContentDTO;
import com.reddiax.rdxvideo.model.dto.WebpageContentRequest;

import java.util.List;

public interface WebpageContentService {
    List<WebpageContentDTO> getAll();
    WebpageContentDTO getById(Long id);
    WebpageContentDTO create(WebpageContentRequest request);
    WebpageContentDTO update(Long id, WebpageContentRequest request);
    void delete(Long id);
}
