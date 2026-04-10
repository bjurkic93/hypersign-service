package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.PlaylistCreateRequest;
import com.reddiax.rdxvideo.model.dto.PlaylistDTO;
import com.reddiax.rdxvideo.model.dto.PlaylistItemRequest;

import java.util.List;

public interface PlaylistService {
    List<PlaylistDTO> getAllPlaylists();
    PlaylistDTO getPlaylist(Long id);
    PlaylistDTO createPlaylist(PlaylistCreateRequest request);
    PlaylistDTO updatePlaylist(Long id, PlaylistCreateRequest request);
    void deletePlaylist(Long id);
    PlaylistDTO toggleActive(Long id);
    List<PlaylistDTO> getPlaylistsByMediaId(Long mediaId);
    PlaylistDTO setPlaylistItems(Long playlistId, List<PlaylistItemRequest> items);
}
