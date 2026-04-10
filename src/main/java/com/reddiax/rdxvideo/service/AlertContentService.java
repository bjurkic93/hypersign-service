package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.AlertContentDTO;
import com.reddiax.rdxvideo.model.dto.AlertContentRequest;

import java.util.List;

public interface AlertContentService {
    List<AlertContentDTO> getAll();
    AlertContentDTO getById(Long id);
    AlertContentDTO create(AlertContentRequest request);
    AlertContentDTO update(Long id, AlertContentRequest request);
    void delete(Long id);
}
