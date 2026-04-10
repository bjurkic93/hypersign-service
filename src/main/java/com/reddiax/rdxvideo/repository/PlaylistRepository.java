package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {
    List<PlaylistEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<PlaylistEntity> findByOrganizationIdAndActiveOrderByCreatedAtDesc(Long organizationId, Boolean active);
    List<PlaylistEntity> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT p FROM PlaylistEntity p JOIN p.items i WHERE i.media.id = :mediaId ORDER BY p.name")
    List<PlaylistEntity> findByMediaId(@Param("mediaId") Long mediaId);

    @Query("SELECT DISTINCT p FROM PlaylistEntity p LEFT JOIN FETCH p.layout WHERE p.id = :id")
    Optional<PlaylistEntity> findByIdWithLayout(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM PlaylistEntity p LEFT JOIN FETCH p.layout ORDER BY p.createdAt DESC")
    List<PlaylistEntity> findAllWithLayoutOrderByCreatedAtDesc();
}
