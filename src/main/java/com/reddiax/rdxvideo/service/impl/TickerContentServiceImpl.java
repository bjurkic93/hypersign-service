package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.TickerContentDTO;
import com.reddiax.rdxvideo.model.dto.TickerContentRequest;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.TickerContentEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.repository.TickerContentRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.TickerContentService;
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
public class TickerContentServiceImpl implements TickerContentService {

    private final TickerContentRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TickerContentDTO> getAll() {
        Long orgId = getCurrentOrganizationId();
        List<TickerContentEntity> entities;
        if (orgId != null) {
            entities = repository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
        } else {
            entities = repository.findAllByOrderByCreatedAtDesc();
        }
        return entities.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TickerContentDTO getById(Long id) {
        TickerContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker content not found"));
        verifyAccess(entity);
        return toDTO(entity);
    }

    @Override
    @Transactional
    public TickerContentDTO create(TickerContentRequest request) {
        TickerContentEntity entity = TickerContentEntity.builder()
                .name(request.getName())
                .text(request.getText())
                .speed(request.getSpeed() != null ? request.getSpeed() : 50)
                .direction(request.getDirection() != null ? request.getDirection() : "LEFT")
                .backgroundColor(request.getBackgroundColor())
                .textColor(request.getTextColor())
                .fontFamily(request.getFontFamily())
                .fontSize(request.getFontSize())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .organization(getCurrentOrganization())
                .build();

        entity = repository.save(entity);
        log.info("Created ticker content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public TickerContentDTO update(Long id, TickerContentRequest request) {
        TickerContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker content not found"));
        verifyAccess(entity);

        entity.setName(request.getName());
        entity.setText(request.getText());
        if (request.getSpeed() != null) entity.setSpeed(request.getSpeed());
        if (request.getDirection() != null) entity.setDirection(request.getDirection());
        entity.setBackgroundColor(request.getBackgroundColor());
        entity.setTextColor(request.getTextColor());
        entity.setFontFamily(request.getFontFamily());
        entity.setFontSize(request.getFontSize());
        if (request.getTags() != null) entity.setTags(new ArrayList<>(request.getTags()));

        entity = repository.save(entity);
        log.info("Updated ticker content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TickerContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker content not found"));
        verifyAccess(entity);
        repository.delete(entity);
        log.info("Deleted ticker content with id: {}", id);
    }

    private void verifyAccess(TickerContentEntity entity) {
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

    private TickerContentDTO toDTO(TickerContentEntity entity) {
        return TickerContentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .text(entity.getText())
                .speed(entity.getSpeed())
                .direction(entity.getDirection())
                .backgroundColor(entity.getBackgroundColor())
                .textColor(entity.getTextColor())
                .fontFamily(entity.getFontFamily())
                .fontSize(entity.getFontSize())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }
}
