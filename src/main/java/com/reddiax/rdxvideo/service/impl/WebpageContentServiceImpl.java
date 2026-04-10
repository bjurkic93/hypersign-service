package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.WebpageContentDTO;
import com.reddiax.rdxvideo.model.dto.WebpageContentRequest;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.model.entity.WebpageContentEntity;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.repository.WebpageContentRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.WebpageContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebpageContentServiceImpl implements WebpageContentService {

    private final WebpageContentRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WebpageContentDTO> getAll() {
        Long orgId = getCurrentOrganizationId();
        List<WebpageContentEntity> entities;
        if (orgId != null) {
            entities = repository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
        } else {
            entities = repository.findAllByOrderByCreatedAtDesc();
        }
        return entities.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WebpageContentDTO getById(Long id) {
        WebpageContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Webpage content not found"));
        verifyAccess(entity);
        return toDTO(entity);
    }

    @Override
    @Transactional
    public WebpageContentDTO create(WebpageContentRequest request) {
        WebpageContentEntity entity = WebpageContentEntity.builder()
                .name(request.getName())
                .url(request.getUrl())
                .refreshIntervalSeconds(request.getRefreshIntervalSeconds() != null ? request.getRefreshIntervalSeconds() : 0)
                .scrollEnabled(request.getScrollEnabled() != null ? request.getScrollEnabled() : false)
                .zoomLevel(request.getZoomLevel() != null ? request.getZoomLevel() : 100)
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .organization(getCurrentOrganization())
                .build();

        entity = repository.save(entity);
        log.info("Created webpage content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public WebpageContentDTO update(Long id, WebpageContentRequest request) {
        WebpageContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Webpage content not found"));
        verifyAccess(entity);

        entity.setName(request.getName());
        entity.setUrl(request.getUrl());
        if (request.getRefreshIntervalSeconds() != null) entity.setRefreshIntervalSeconds(request.getRefreshIntervalSeconds());
        if (request.getScrollEnabled() != null) entity.setScrollEnabled(request.getScrollEnabled());
        if (request.getZoomLevel() != null) entity.setZoomLevel(request.getZoomLevel());
        if (request.getTags() != null) entity.setTags(new ArrayList<>(request.getTags()));

        entity = repository.save(entity);
        log.info("Updated webpage content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WebpageContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Webpage content not found"));
        verifyAccess(entity);
        repository.delete(entity);
        log.info("Deleted webpage content with id: {}", id);
    }

    private void verifyAccess(WebpageContentEntity entity) {
        Long orgId = getCurrentOrganizationId();
        if (orgId != null && entity.getOrganization() != null && !orgId.equals(entity.getOrganization().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
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

    private WebpageContentDTO toDTO(WebpageContentEntity entity) {
        return WebpageContentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(entity.getUrl())
                .refreshIntervalSeconds(entity.getRefreshIntervalSeconds())
                .scrollEnabled(entity.getScrollEnabled())
                .zoomLevel(entity.getZoomLevel())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }
}
