package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.PlaylistCreateRequest;
import com.reddiax.rdxvideo.model.dto.PlaylistDTO;
import com.reddiax.rdxvideo.model.dto.PlaylistItemRequest;
import com.reddiax.rdxvideo.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/playlists")
@Tag(name = "Playlist Management", description = "API for managing playlists")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    @Operation(summary = "Get all playlists")
    public List<PlaylistDTO> getAllPlaylists(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all playlists", jwt.getSubject());
        return playlistService.getAllPlaylists();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get playlist by ID")
    public PlaylistDTO getPlaylist(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching playlist: {}", jwt.getSubject(), id);
        return playlistService.getPlaylist(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new playlist")
    public PlaylistDTO createPlaylist(
            @Valid @RequestBody PlaylistCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating playlist: {}", jwt.getSubject(), request.getName());
        return playlistService.createPlaylist(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a playlist")
    public PlaylistDTO updatePlaylist(
            @PathVariable Long id,
            @Valid @RequestBody PlaylistCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating playlist: {}", jwt.getSubject(), id);
        return playlistService.updatePlaylist(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a playlist")
    public void deletePlaylist(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting playlist: {}", jwt.getSubject(), id);
        playlistService.deletePlaylist(id);
    }

    @PostMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle playlist active status")
    public PlaylistDTO toggleActive(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} toggling playlist active status: {}", jwt.getSubject(), id);
        return playlistService.toggleActive(id);
    }

    @GetMapping("/by-media/{mediaId}")
    @Operation(summary = "Get playlists containing a specific media")
    public List<PlaylistDTO> getPlaylistsByMedia(@PathVariable Long mediaId, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching playlists for media: {}", jwt.getSubject(), mediaId);
        return playlistService.getPlaylistsByMediaId(mediaId);
    }

    @PutMapping("/{id}/items")
    @Operation(summary = "Set playlist items (replace all items)")
    public PlaylistDTO setPlaylistItems(
            @PathVariable Long id,
            @Valid @RequestBody List<PlaylistItemRequest> items,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} setting {} items for playlist: {}", jwt.getSubject(), items.size(), id);
        return playlistService.setPlaylistItems(id, items);
    }
}
