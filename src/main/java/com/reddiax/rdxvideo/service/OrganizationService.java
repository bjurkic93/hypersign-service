package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.OrganizationDTO;
import com.reddiax.rdxvideo.model.dto.OrganizationRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrganizationService {

    Page<OrganizationDTO> getOrganizations(Pageable pageable);

    OrganizationDTO getOrganization(Long id);

    OrganizationDTO getOrganizationByCode(String code);

    OrganizationDTO createOrganization(OrganizationRequestDTO request);

    OrganizationDTO updateOrganization(Long id, OrganizationRequestDTO request);

    void deleteOrganization(Long id);
}
