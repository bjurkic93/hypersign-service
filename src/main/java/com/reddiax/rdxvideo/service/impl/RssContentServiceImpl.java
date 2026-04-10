package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.RssContentDTO;
import com.reddiax.rdxvideo.model.dto.RssContentRequest;
import com.reddiax.rdxvideo.model.dto.RssItemDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.RssContentEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.repository.RssContentRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.RssContentService;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssContentServiceImpl implements RssContentService {

    private final RssContentRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RssContentDTO> getAll() {
        Long orgId = getCurrentOrganizationId();
        List<RssContentEntity> entities;
        if (orgId != null) {
            entities = repository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
        } else {
            entities = repository.findAllByOrderByCreatedAtDesc();
        }
        return entities.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RssContentDTO getById(Long id) {
        RssContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSS content not found"));
        verifyAccess(entity);
        return toDTO(entity);
    }

    @Override
    @Transactional
    public RssContentDTO create(RssContentRequest request) {
        RssContentEntity entity = RssContentEntity.builder()
                .name(request.getName())
                .feedUrl(request.getFeedUrl())
                .refreshIntervalMinutes(request.getRefreshIntervalMinutes() != null ? request.getRefreshIntervalMinutes() : 15)
                .maxItems(request.getMaxItems() != null ? request.getMaxItems() : 10)
                .showImages(request.getShowImages() != null ? request.getShowImages() : true)
                .showDescription(request.getShowDescription() != null ? request.getShowDescription() : true)
                .displayMode(request.getDisplayMode() != null ? request.getDisplayMode() : "LIST")
                .backgroundColor(request.getBackgroundColor())
                .textColor(request.getTextColor())
                .fontFamily(request.getFontFamily())
                .fontSize(request.getFontSize())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .organization(getCurrentOrganization())
                .build();

        entity = repository.save(entity);
        log.info("Created RSS content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public RssContentDTO update(Long id, RssContentRequest request) {
        RssContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSS content not found"));
        verifyAccess(entity);

        entity.setName(request.getName());
        entity.setFeedUrl(request.getFeedUrl());
        if (request.getRefreshIntervalMinutes() != null) entity.setRefreshIntervalMinutes(request.getRefreshIntervalMinutes());
        if (request.getMaxItems() != null) entity.setMaxItems(request.getMaxItems());
        if (request.getShowImages() != null) entity.setShowImages(request.getShowImages());
        if (request.getShowDescription() != null) entity.setShowDescription(request.getShowDescription());
        if (request.getDisplayMode() != null) entity.setDisplayMode(request.getDisplayMode());
        entity.setBackgroundColor(request.getBackgroundColor());
        entity.setTextColor(request.getTextColor());
        entity.setFontFamily(request.getFontFamily());
        entity.setFontSize(request.getFontSize());
        if (request.getTags() != null) entity.setTags(new ArrayList<>(request.getTags()));

        entity = repository.save(entity);
        log.info("Updated RSS content with id: {}", entity.getId());
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RssContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSS content not found"));
        verifyAccess(entity);
        repository.delete(entity);
        log.info("Deleted RSS content with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RssItemDTO> fetchRssItems(Long id) {
        RssContentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSS content not found"));
        verifyAccess(entity);
        return previewRssFeed(entity.getFeedUrl(), entity.getMaxItems() != null ? entity.getMaxItems() : 10);
    }

    @Override
    public List<RssItemDTO> previewRssFeed(String feedUrl, int maxItems) {
        try {
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));
            
            List<SyndEntry> entries = feed.getEntries();
            int limit = Math.min(entries.size(), maxItems);
            
            List<RssItemDTO> items = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                SyndEntry entry = entries.get(i);
                String imageUrl = extractImageUrl(entry);
                String description = entry.getDescription() != null ? 
                        stripHtml(entry.getDescription().getValue()) : null;
                
                items.add(RssItemDTO.builder()
                        .title(stripHtml(entry.getTitle()))
                        .description(description)
                        .link(entry.getLink())
                        .imageUrl(imageUrl)
                        .pubDate(entry.getPublishedDate() != null ? entry.getPublishedDate().toString() : null)
                        .build());
            }
            
            return items;
        } catch (Exception e) {
            log.error("Failed to parse RSS feed: {}", feedUrl, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse RSS feed: " + e.getMessage());
        }
    }

    private String stripHtml(String html) {
        if (html == null || html.isEmpty()) return html;
        // Remove HTML tags and decode entities
        return html.replaceAll("<[^>]*>", "")
                   .replaceAll("&nbsp;", " ")
                   .replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">")
                   .replaceAll("&quot;", "\"")
                   .replaceAll("&#39;", "'")
                   .trim();
    }

    private String extractImageUrl(SyndEntry entry) {
        // Try to get image from enclosures
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            return entry.getEnclosures().stream()
                    .filter(e -> e.getType() != null && e.getType().startsWith("image/"))
                    .findFirst()
                    .map(e -> e.getUrl())
                    .orElse(null);
        }
        
        // Try media:content or media:thumbnail via foreign markup
        if (entry.getForeignMarkup() != null) {
            for (org.jdom2.Element element : entry.getForeignMarkup()) {
                if ("content".equals(element.getName()) || "thumbnail".equals(element.getName())) {
                    String url = element.getAttributeValue("url");
                    if (url != null && !url.isEmpty()) {
                        return url;
                    }
                }
            }
        }
        
        // Try to extract from description HTML
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            String desc = entry.getDescription().getValue();
            int imgStart = desc.indexOf("<img");
            if (imgStart >= 0) {
                int srcStart = desc.indexOf("src=\"", imgStart);
                if (srcStart >= 0) {
                    srcStart += 5;
                    int srcEnd = desc.indexOf("\"", srcStart);
                    if (srcEnd > srcStart) {
                        return desc.substring(srcStart, srcEnd);
                    }
                }
            }
        }
        
        return null;
    }

    private void verifyAccess(RssContentEntity entity) {
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

    private RssContentDTO toDTO(RssContentEntity entity) {
        return RssContentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .feedUrl(entity.getFeedUrl())
                .refreshIntervalMinutes(entity.getRefreshIntervalMinutes())
                .maxItems(entity.getMaxItems())
                .showImages(entity.getShowImages())
                .showDescription(entity.getShowDescription())
                .displayMode(entity.getDisplayMode())
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
