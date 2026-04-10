package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.OrganizationDTO;
import com.reddiax.rdxvideo.model.dto.OrganizationRequestDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.mapper.OrganizationMapper;
import com.reddiax.rdxvideo.repository.OrganizationRepository;
import com.reddiax.rdxvideo.service.MediaService;
import com.reddiax.rdxvideo.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MediaService mediaService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationDTO> getOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable)
                .map(this::toOrgDtoWithResolvedUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDTO getOrganization(Long id) {
        return toOrgDtoWithResolvedUrl(findOrganizationById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDTO getOrganizationByCode(String code) {
        return toOrgDtoWithResolvedUrl(
                organizationRepository.findByCode(code)
                        .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                                "Organization not found with code: " + code, "ORGANIZATION_NOT_FOUND"))
        );
    }

    @Override
    @Transactional
    public OrganizationDTO createOrganization(OrganizationRequestDTO request) {
        if (organizationRepository.existsByCode(request.getCode())) {
            throw new RdXException(HttpStatus.CONFLICT, 
                    "Organization with code already exists: " + request.getCode(), "ORGANIZATION_CODE_EXISTS");
        }

        OrganizationEntity entity = OrganizationMapper.INSTANCE.toCreateEntity(request);
        OrganizationEntity savedOrg = organizationRepository.save(entity);
        
        return toOrgDtoWithResolvedUrl(savedOrg);
    }

    @Override
    @Transactional
    public OrganizationDTO updateOrganization(Long id, OrganizationRequestDTO request) {
        OrganizationEntity existingEntity = findOrganizationById(id);

        // Check if code is being changed and if new code already exists
        if (!existingEntity.getCode().equals(request.getCode()) 
                && organizationRepository.existsByCode(request.getCode())) {
            throw new RdXException(HttpStatus.CONFLICT, 
                    "Organization with code already exists: " + request.getCode(), "ORGANIZATION_CODE_EXISTS");
        }

        OrganizationMapper.INSTANCE.updateEntity(request, existingEntity);
        
        // Handle logo image id update
        if (request.getLogoImageId() != null) {
            if (request.getLogoImageId() <= 0) {
                existingEntity.setLogoImageId(null);
            } else {
                existingEntity.setLogoImageId(request.getLogoImageId());
            }
        }
        
        return toOrgDtoWithResolvedUrl(organizationRepository.save(existingEntity));
    }

    @Override
    @Transactional
    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new RdXException(HttpStatus.NOT_FOUND, 
                    "Organization not found with id: " + id, "ORGANIZATION_NOT_FOUND");
        }
        organizationRepository.deleteById(id);
    }

    private OrganizationEntity findOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, 
                        "Organization not found with id: " + id, "ORGANIZATION_NOT_FOUND"));
    }

    /**
     * Convert OrganizationEntity to OrganizationDTO with resolved logo URL.
     */
    private OrganizationDTO toOrgDtoWithResolvedUrl(OrganizationEntity entity) {
        OrganizationDTO dto = OrganizationMapper.INSTANCE.toDto(entity);
        
        // Resolve logo URL from imageId
        if (entity.getLogoImageId() != null) {
            try {
                String url = mediaService.getMediaUrl(entity.getLogoImageId()).getUrl();
                dto.setLogoUrl(url);
            } catch (Exception e) {
                log.warn("Failed to resolve logo URL for organization {}: {}", entity.getId(), e.getMessage());
                dto.setLogoUrl(null);
            }
        }
        
        return dto;
    }
}
