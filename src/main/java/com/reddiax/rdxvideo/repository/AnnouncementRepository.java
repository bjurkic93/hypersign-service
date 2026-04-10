package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.constant.AnnouncementStatusEnum;
import com.reddiax.rdxvideo.model.entity.AnnouncementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    Page<AnnouncementEntity> findByActiveTrue(Pageable pageable);

    Page<AnnouncementEntity> findByActiveTrueAndStatus(AnnouncementStatusEnum status, Pageable pageable);

    @Query("SELECT a FROM AnnouncementEntity a WHERE a.active = true AND a.status = :status " +
           "AND a.scheduledAt IS NOT NULL AND a.scheduledAt <= :now")
    List<AnnouncementEntity> findScheduledAnnouncementsReadyToSend(
            @Param("status") AnnouncementStatusEnum status,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(a) FROM AnnouncementEntity a WHERE a.active = true AND a.status = :status")
    long countByStatus(@Param("status") AnnouncementStatusEnum status);
}
