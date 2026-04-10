package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.LayoutCreateRequest;
import com.reddiax.rdxvideo.model.dto.LayoutDTO;
import com.reddiax.rdxvideo.service.LayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/layouts")
@RequiredArgsConstructor
@Slf4j
public class LayoutController {

    private final LayoutService layoutService;

    @GetMapping
    public ResponseEntity<List<LayoutDTO>> getAllLayouts() {
        log.debug("GET /api/v1/layouts - Fetching all layouts");
        List<LayoutDTO> layouts = layoutService.getAllLayouts();
        return ResponseEntity.ok(layouts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LayoutDTO> getLayoutById(@PathVariable Long id) {
        log.debug("GET /api/v1/layouts/{} - Fetching layout by id", id);
        LayoutDTO layout = layoutService.getLayoutById(id);
        return ResponseEntity.ok(layout);
    }

    @PostMapping
    public ResponseEntity<LayoutDTO> createLayout(@Valid @RequestBody LayoutCreateRequest request) {
        log.debug("POST /api/v1/layouts - Creating layout: {}", request.getName());
        LayoutDTO created = layoutService.createLayout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LayoutDTO> updateLayout(
            @PathVariable Long id,
            @Valid @RequestBody LayoutCreateRequest request) {
        log.debug("PUT /api/v1/layouts/{} - Updating layout", id);
        LayoutDTO updated = layoutService.updateLayout(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLayout(@PathVariable Long id) {
        log.debug("DELETE /api/v1/layouts/{} - Deleting layout", id);
        layoutService.deleteLayout(id);
        return ResponseEntity.noContent().build();
    }
}
