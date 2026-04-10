package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.model.dto.AdvertisementDeviceAnalyticsDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementSummaryDTO;
import com.reddiax.rdxvideo.model.dto.AdvertisementTimelineDTO;
import com.reddiax.rdxvideo.repository.AdvertisementVerificationLogRepository;
import com.reddiax.rdxvideo.service.VerificationAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationAnalyticsServiceImpl implements VerificationAnalyticsService {

    private final AdvertisementVerificationLogRepository verificationLogRepository;

    @Override
    public List<AdvertisementTimelineDTO> getTimeline(Long advertisementId, LocalDateTime startDate, LocalDateTime endDate, String interval) {
        log.debug("Getting timeline for advertisement {} from {} to {} with interval {}", advertisementId, startDate, endDate, interval);
        
        List<Object[]> rawData;
        
        if ("DAY".equalsIgnoreCase(interval)) {
            rawData = verificationLogRepository.getDailyTimelineByAdvertisement(advertisementId, startDate, endDate);
        } else {
            // Default to hourly
            rawData = verificationLogRepository.getHourlyTimelineByAdvertisement(advertisementId, startDate, endDate);
        }
        
        List<AdvertisementTimelineDTO> timeline = new ArrayList<>();
        
        for (Object[] row : rawData) {
            LocalDateTime timestamp = null;
            if (row[0] instanceof Timestamp ts) {
                timestamp = ts.toLocalDateTime();
            }
            
            AdvertisementTimelineDTO dto = AdvertisementTimelineDTO.builder()
                    .timestamp(timestamp)
                    .verificationCount(toLong(row[1]))
                    .successCount(toLong(row[2]))
                    .failCount(toLong(row[3]))
                    .uniqueDevices(toLong(row[4]))
                    .build();
            
            timeline.add(dto);
        }
        
        return timeline;
    }

    @Override
    public List<AdvertisementDeviceAnalyticsDTO> getDeviceAnalytics(Long advertisementId, int limit) {
        log.debug("Getting device analytics for advertisement {} with limit {}", advertisementId, limit);
        
        List<Object[]> rawData = verificationLogRepository.getDeviceAnalyticsByAdvertisement(advertisementId, PageRequest.of(0, limit));
        
        List<AdvertisementDeviceAnalyticsDTO> devices = new ArrayList<>();
        
        for (Object[] row : rawData) {
            LocalDateTime lastVerifiedAt = null;
            if (row[4] instanceof Timestamp ts) {
                lastVerifiedAt = ts.toLocalDateTime();
            }
            
            AdvertisementDeviceAnalyticsDTO dto = AdvertisementDeviceAnalyticsDTO.builder()
                    .userAdvertisementId(toLong(row[0]))
                    .deviceInfo((String) row[1])
                    .totalVerifications(toLong(row[2]))
                    .totalSeconds(toLong(row[3]))
                    .lastVerifiedAt(lastVerifiedAt)
                    .build();
            
            devices.add(dto);
        }
        
        return devices;
    }

    @Override
    public AdvertisementSummaryDTO getSummary(Long advertisementId) {
        log.debug("Getting summary for advertisement {}", advertisementId);
        
        List<Object[]> results = verificationLogRepository.getAdvertisementSummary(advertisementId);
        
        if (results == null || results.isEmpty()) {
            return AdvertisementSummaryDTO.builder()
                    .totalDisplayTime(0L)
                    .averageDisplayTimePerDevice(0.0)
                    .activeDevices(0L)
                    .totalVerifications(0L)
                    .successfulVerifications(0L)
                    .successRate(0.0)
                    .build();
        }
        
        Object[] row = results.get(0);
        
        Long totalDisplayTime = toLong(row[0]);
        Long activeDevices = toLong(row[1]);
        Long totalVerifications = toLong(row[2]);
        Long successfulVerifications = toLong(row[3]);
        
        Double averageDisplayTime = activeDevices > 0 
                ? totalDisplayTime.doubleValue() / activeDevices 
                : 0.0;
        
        Double successRate = totalVerifications > 0 
                ? (successfulVerifications.doubleValue() / totalVerifications) * 100 
                : 0.0;
        
        return AdvertisementSummaryDTO.builder()
                .totalDisplayTime(totalDisplayTime)
                .averageDisplayTimePerDevice(averageDisplayTime)
                .activeDevices(activeDevices)
                .totalVerifications(totalVerifications)
                .successfulVerifications(successfulVerifications)
                .successRate(Math.round(successRate * 100.0) / 100.0) // Round to 2 decimal places
                .build();
    }
    
    /**
     * Helper method to convert various numeric types to Long.
     */
    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof BigInteger bi) return bi.longValue();
        if (value instanceof BigDecimal bd) return bd.longValue();
        if (value instanceof Number n) return n.longValue();
        return 0L;
    }
}
