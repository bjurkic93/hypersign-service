package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.TickerContentDTO;
import com.reddiax.rdxvideo.model.dto.TickerContentRequest;

import java.util.List;

public interface TickerContentService {
    List<TickerContentDTO> getAll();
    TickerContentDTO getById(Long id);
    TickerContentDTO create(TickerContentRequest request);
    TickerContentDTO update(Long id, TickerContentRequest request);
    void delete(Long id);
}
