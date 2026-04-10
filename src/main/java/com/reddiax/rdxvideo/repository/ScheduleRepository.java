package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByOrganizationIdOrderByPriorityDescCreatedAtDesc(Long organizationId);
    List<ScheduleEntity> findAllByOrderByPriorityDescCreatedAtDesc();
    
    @Query("SELECT s FROM ScheduleEntity s WHERE s.active = true " +
           "AND s.startDate <= :date AND (s.endDate IS NULL OR s.endDate >= :date) " +
           "AND s.startTime <= :time AND s.endTime >= :time " +
           "ORDER BY s.priority DESC")
    List<ScheduleEntity> findActiveSchedulesForDateTime(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );
    
    List<ScheduleEntity> findByPlaylistId(Long playlistId);
}
