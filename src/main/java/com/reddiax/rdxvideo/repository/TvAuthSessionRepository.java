package com.reddiax.rdxvideo.repository;

import com.reddiax.rdxvideo.model.entity.TvAuthSessionEntity;
import com.reddiax.rdxvideo.model.entity.TvAuthSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TvAuthSessionRepository extends JpaRepository<TvAuthSessionEntity, Long> {
    
    Optional<TvAuthSessionEntity> findBySessionId(String sessionId);
    
    Optional<TvAuthSessionEntity> findBySessionCode(String sessionCode);
    
    Optional<TvAuthSessionEntity> findBySessionIdAndStatus(String sessionId, TvAuthSessionStatus status);
    
    Optional<TvAuthSessionEntity> findBySessionCodeAndStatus(String sessionCode, TvAuthSessionStatus status);
    
    /**
     * Find active (pending, not expired) session by device ID.
     */
    @Query("SELECT s FROM TvAuthSessionEntity s WHERE s.deviceId = :deviceId AND s.status = 'PENDING' AND s.expiresAt > :now")
    Optional<TvAuthSessionEntity> findActiveSessionByDeviceId(String deviceId, LocalDateTime now);
    
    /**
     * Expire old pending sessions.
     */
    @Modifying
    @Query("UPDATE TvAuthSessionEntity s SET s.status = 'EXPIRED' WHERE s.status = 'PENDING' AND s.expiresAt < :now")
    int expireOldSessions(LocalDateTime now);
}
