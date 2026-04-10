package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.PlaylistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItemEntity, Long> {
    List<PlaylistItemEntity> findByPlaylistIdOrderByOrderIndexAsc(Long playlistId);
    void deleteByPlaylistId(Long playlistId);
}
