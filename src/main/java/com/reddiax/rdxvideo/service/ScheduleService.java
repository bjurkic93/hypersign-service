package com.reddiax.rdxvideo.service;

import com.reddiax.rdxvideo.model.dto.ScheduleCreateRequest;
import com.reddiax.rdxvideo.model.dto.ScheduleDTO;

import java.util.List;

public interface ScheduleService {
    List<ScheduleDTO> getAllSchedules();
    ScheduleDTO getSchedule(Long id);
    ScheduleDTO createSchedule(ScheduleCreateRequest request);
    ScheduleDTO updateSchedule(Long id, ScheduleCreateRequest request);
    void deleteSchedule(Long id);
    ScheduleDTO toggleActive(Long id);
    ScheduleDTO getCurrentActiveSchedule();
}
