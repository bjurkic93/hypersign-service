package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.LayoutCreateRequest;
import com.reddiax.rdxvideo.model.dto.LayoutDTO;
import com.reddiax.rdxvideo.model.dto.LayoutSectionDTO;
import com.reddiax.rdxvideo.model.entity.LayoutEntity;
import com.reddiax.rdxvideo.model.entity.LayoutSectionEntity;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.repository.LayoutRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.LayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LayoutServiceImpl implements LayoutService {

    private final LayoutRepository layoutRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LayoutDTO> getAllLayouts() {
        Long orgId = getCurrentOrganizationId();
        if (orgId != null) {
            return getLayoutsByOrganization(orgId);
        }
        return layoutRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LayoutDTO> getLayoutsByOrganization(Long organizationId) {
        return layoutRepository.findByOrganization_IdOrderByCreatedAtDesc(organizationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LayoutDTO getLayoutById(Long id) {
        LayoutEntity entity = layoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Layout not found with id: " + id));
        
        // Verify organization access
        Long orgId = getCurrentOrganizationId();
        if (orgId != null && entity.getOrganization() != null && !orgId.equals(entity.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this layout");
        }
        
        return toDTO(entity);
    }

    @Override
    @Transactional
    public LayoutDTO createLayout(LayoutCreateRequest request) {
        LayoutEntity entity = new LayoutEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setWidth(request.getWidth());
        entity.setHeight(request.getHeight());
        entity.setOrientation(LayoutEntity.LayoutOrientation.valueOf(request.getOrientation().toUpperCase()));
        entity.setSections(new ArrayList<>());

        // Set organization from current user
        OrganizationEntity org = getCurrentOrganization();
        if (org != null) {
            entity.setOrganization(org);
        }

        if (request.getSections() != null) {
            for (LayoutSectionDTO sectionDTO : request.getSections()) {
                LayoutSectionEntity section = toSectionEntity(sectionDTO);
                section.setLayout(entity);
                entity.getSections().add(section);
            }
        }

        LayoutEntity saved = layoutRepository.save(entity);
        log.info("Created layout with id: {} for organization: {}", saved.getId(), 
                org != null ? org.getId() : "none");
        return toDTO(saved);
    }

    @Override
    @Transactional
    public LayoutDTO updateLayout(Long id, LayoutCreateRequest request) {
        LayoutEntity entity = layoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Layout not found with id: " + id));

        // Verify organization access
        Long orgId = getCurrentOrganizationId();
        if (orgId != null && entity.getOrganization() != null && !orgId.equals(entity.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this layout");
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setWidth(request.getWidth());
        entity.setHeight(request.getHeight());
        entity.setOrientation(LayoutEntity.LayoutOrientation.valueOf(request.getOrientation().toUpperCase()));

        entity.getSections().clear();

        if (request.getSections() != null) {
            for (LayoutSectionDTO sectionDTO : request.getSections()) {
                LayoutSectionEntity section = toSectionEntity(sectionDTO);
                section.setLayout(entity);
                entity.getSections().add(section);
            }
        }

        LayoutEntity saved = layoutRepository.save(entity);
        log.info("Updated layout with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteLayout(Long id) {
        LayoutEntity entity = layoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Layout not found with id: " + id));
        
        // Verify organization access
        Long orgId = getCurrentOrganizationId();
        if (orgId != null && entity.getOrganization() != null && !orgId.equals(entity.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this layout");
        }
        
        layoutRepository.deleteById(id);
        log.info("Deleted layout with id: {}", id);
    }

    private Long getCurrentOrganizationId() {
        OrganizationEntity org = getCurrentOrganization();
        return org != null ? org.getId() : null;
    }

    private OrganizationEntity getCurrentOrganization() {
        return SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findByExternalId)
                .map(UserEntity::getOrganization)
                .orElse(null);
    }

    private LayoutDTO toDTO(LayoutEntity entity) {
        return LayoutDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .orientation(entity.getOrientation().name().toLowerCase())
                .sections(entity.getSections().stream()
                        .map(this::toSectionDTO)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getModifiedAt())
                .build();
    }

    private LayoutSectionDTO toSectionDTO(LayoutSectionEntity entity) {
        return LayoutSectionDTO.builder()
                .id(entity.getSectionId())
                .name(entity.getName())
                .x(entity.getX())
                .y(entity.getY())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .contentType(entity.getContentType().name().toLowerCase())
                .color(entity.getColor())
                .zIndex(entity.getZIndex())
                .build();
    }

    private LayoutSectionEntity toSectionEntity(LayoutSectionDTO dto) {
        LayoutSectionEntity entity = new LayoutSectionEntity();
        entity.setSectionId(dto.getId());
        entity.setName(dto.getName());
        entity.setX(dto.getX());
        entity.setY(dto.getY());
        entity.setWidth(dto.getWidth());
        entity.setHeight(dto.getHeight());
        entity.setContentType(LayoutSectionEntity.ContentType.valueOf(dto.getContentType().toUpperCase()));
        entity.setColor(dto.getColor());
        entity.setZIndex(dto.getZIndex());
        return entity;
    }
}
