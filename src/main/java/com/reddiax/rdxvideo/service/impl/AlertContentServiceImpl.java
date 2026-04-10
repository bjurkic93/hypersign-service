package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.AlertContentDTO;
import com.reddiax.rdxvideo.model.dto.AlertContentRequest;
import com.reddiax.rdxvideo.model.entity.AlertContentEntity;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.repository.AlertContentRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.AlertContentService;
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
public class AlertContentServiceImpl implements AlertContentService {

    private final AlertContentRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertContentDTO> getAll() {
        Long orgId = getCurrentOrganizationId();
        List<AlertContentEntity> entities;
        if (orgId != null) {
            entities = repository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
        } else {
            entities = repository.findAllByOrderByCreatedAtDesc();
        }
        return entities.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertContentDTO getById(Long id) {
        AlertContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert content not found"));
        verifyAccess(entity);
        return toDTO(entity);
    }

    @Override
    @Transactional
    public AlertContentDTO create(AlertContentRequest request) {
        AlertContentEntity entity = AlertContentEntity.builder()
                .name(request.getName())
                .title(request.getTitle())
                .message(request.getMessage())
                .severity(request.getSeverity() != null ? request.getSeverity() : "WARNING")
                .displayMode(request.getDisplayMode() != null ? request.getDisplayMode() : "BANNER")
                .backgroundColor(request.getBackgroundColor())
                .textColor(request.getTextColor())
                .iconName(request.getIconName())
                .showIcon(request.getShowIcon() != null ? request.getShowIcon() : true)
                .autoScroll(request.getAutoScroll() != null ? request.getAutoScroll() : false)
                .scrollSpeed(request.getScrollSpeed())
                .soundEnabled(request.getSoundEnabled() != null ? request.getSoundEnabled() : false)
                .soundUrl(request.getSoundUrl())
                .blinkEnabled(request.getBlinkEnabled() != null ? request.getBlinkEnabled() : false)
                .activeFrom(request.getActiveFrom())
                .activeUntil(request.getActiveUntil())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .organization(getCurrentOrganization())
                .build();

        entity = repository.save(entity);
        log.info("Created Alert content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public AlertContentDTO update(Long id, AlertContentRequest request) {
        AlertContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert content not found"));
        verifyAccess(entity);

        entity.setName(request.getName());
        entity.setTitle(request.getTitle());
        entity.setMessage(request.getMessage());
        if (request.getSeverity() != null) entity.setSeverity(request.getSeverity());
        if (request.getDisplayMode() != null) entity.setDisplayMode(request.getDisplayMode());
        entity.setBackgroundColor(request.getBackgroundColor());
        entity.setTextColor(request.getTextColor());
        entity.setIconName(request.getIconName());
        if (request.getShowIcon() != null) entity.setShowIcon(request.getShowIcon());
        if (request.getAutoScroll() != null) entity.setAutoScroll(request.getAutoScroll());
        entity.setScrollSpeed(request.getScrollSpeed());
        if (request.getSoundEnabled() != null) entity.setSoundEnabled(request.getSoundEnabled());
        entity.setSoundUrl(request.getSoundUrl());
        if (request.getBlinkEnabled() != null) entity.setBlinkEnabled(request.getBlinkEnabled());
        entity.setActiveFrom(request.getActiveFrom());
        entity.setActiveUntil(request.getActiveUntil());
        if (request.getTags() != null) entity.setTags(new ArrayList<>(request.getTags()));

        entity = repository.save(entity);
        log.info("Updated Alert content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AlertContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert content not found"));
        verifyAccess(entity);
        repository.delete(entity);
        log.info("Deleted Alert content with id: {}", id);
    }

    private void verifyAccess(AlertContentEntity entity) {
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

    private AlertContentDTO toDTO(AlertContentEntity entity) {
        return AlertContentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .severity(entity.getSeverity())
                .displayMode(entity.getDisplayMode())
                .backgroundColor(entity.getBackgroundColor())
                .textColor(entity.getTextColor())
                .iconName(entity.getIconName())
                .showIcon(entity.getShowIcon())
                .autoScroll(entity.getAutoScroll())
                .scrollSpeed(entity.getScrollSpeed())
                .soundEnabled(entity.getSoundEnabled())
                .soundUrl(entity.getSoundUrl())
                .blinkEnabled(entity.getBlinkEnabled())
                .activeFrom(entity.getActiveFrom())
                .activeUntil(entity.getActiveUntil())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }
}
