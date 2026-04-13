package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.ContentStatusEnum;
import com.reddiax.rdxvideo.constant.ScheduleRepeatType;
import com.reddiax.rdxvideo.model.dto.ScheduleCreateRequest;
import com.reddiax.rdxvideo.model.dto.ScheduleDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.PlaylistEntity;
import com.reddiax.rdxvideo.model.entity.ScheduleEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.repository.PlaylistRepository;
import com.reddiax.rdxvideo.repository.ScheduleRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.PushNotificationService;
import com.reddiax.rdxvideo.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAllByOrderByPriorityDescCreatedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public ScheduleDTO getSchedule(Long id) {
        ScheduleEntity entity = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
        return toDTO(entity);
    }

    @Override
    @Transactional
    public ScheduleDTO createSchedule(ScheduleCreateRequest request) {
        PlaylistEntity playlist = playlistRepository.findById(request.getPlaylistId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist not found"));

        // Get current user's organization
        OrganizationEntity organization = getCurrentUserOrganization();

        ScheduleEntity schedule = ScheduleEntity.builder()
                .name(request.getName())
                .playlist(playlist)
                .organization(organization)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .repeatType(request.getRepeatType() != null ? request.getRepeatType() : ScheduleRepeatType.DAILY)
                .daysOfWeek(request.getDaysOfWeek() != null ? request.getDaysOfWeek() : new HashSet<>())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .active(true)
                .status(request.getStatus() != null ? request.getStatus() : ContentStatusEnum.DRAFT)
                .build();

        log.info("Creating schedule '{}' for organization {}", request.getName(), 
                organization != null ? organization.getId() : "NULL");

        schedule = scheduleRepository.save(schedule);
        
        // Send push notification to all organization devices
        if (organization != null) {
            pushNotificationService.sendContentRefreshToOrganization(organization.getId());
        }
        
        return toDTO(schedule);
    }
    
    private OrganizationEntity getCurrentUserOrganization() {
        String userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
        
        UserEntity user = userRepository.findByExternalId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        return user.getOrganization();
    }

    @Override
    @Transactional
    public ScheduleDTO updateSchedule(Long id, ScheduleCreateRequest request) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        PlaylistEntity playlist = playlistRepository.findById(request.getPlaylistId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist not found"));

        schedule.setName(request.getName());
        schedule.setPlaylist(playlist);
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setRepeatType(request.getRepeatType() != null ? request.getRepeatType() : ScheduleRepeatType.DAILY);
        schedule.setDaysOfWeek(request.getDaysOfWeek() != null ? request.getDaysOfWeek() : new HashSet<>());
        schedule.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }

        schedule = scheduleRepository.save(schedule);
        
        // Send push notification to all organization devices
        if (schedule.getOrganization() != null) {
            pushNotificationService.sendContentRefreshToOrganization(schedule.getOrganization().getId());
        }
        
        return toDTO(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
        }
        scheduleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ScheduleDTO toggleActive(Long id) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
        schedule.setActive(!schedule.getActive());
        schedule = scheduleRepository.save(schedule);
        
        // Send push notification to all organization devices
        if (schedule.getOrganization() != null) {
            pushNotificationService.sendContentRefreshToOrganization(schedule.getOrganization().getId());
        }
        
        return toDTO(schedule);
    }

    @Override
    public ScheduleDTO getCurrentActiveSchedule() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        List<ScheduleEntity> candidates = scheduleRepository.findActiveSchedulesForDateTime(today, now);

        for (ScheduleEntity schedule : candidates) {
            if (isScheduleActiveToday(schedule, dayOfWeek)) {
                return toDTO(schedule);
            }
        }

        return null;
    }

    private boolean isScheduleActiveToday(ScheduleEntity schedule, DayOfWeek today) {
        return switch (schedule.getRepeatType()) {
            case ONCE -> true;
            case DAILY -> true;
            case WEEKLY -> schedule.getDaysOfWeek().contains(today);
            case CUSTOM -> schedule.getDaysOfWeek().contains(today);
        };
    }

    private ScheduleDTO toDTO(ScheduleEntity entity) {
        return ScheduleDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .playlistId(entity.getPlaylist().getId())
                .playlistName(entity.getPlaylist().getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .repeatType(entity.getRepeatType())
                .daysOfWeek(entity.getDaysOfWeek())
                .priority(entity.getPriority())
                .active(entity.getActive())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
