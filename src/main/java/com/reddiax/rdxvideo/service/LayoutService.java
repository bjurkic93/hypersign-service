package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.LayoutCreateRequest;
import com.reddiax.rdxvideo.model.dto.LayoutDTO;

import java.util.List;

public interface LayoutService {
    List<LayoutDTO> getAllLayouts();
    
    List<LayoutDTO> getLayoutsByOrganization(Long organizationId);
    
    LayoutDTO getLayoutById(Long id);
    
    LayoutDTO createLayout(LayoutCreateRequest request);
    
    LayoutDTO updateLayout(Long id, LayoutCreateRequest request);
    
    void deleteLayout(Long id);
}
