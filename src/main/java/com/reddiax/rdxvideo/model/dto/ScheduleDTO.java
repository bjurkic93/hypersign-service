package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentStatusEnum;
import com.reddiax.rdxvideo.constant.ScheduleRepeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long id;
    private String name;
    private Long playlistId;
    private String playlistName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ScheduleRepeatType repeatType;
    private Set<DayOfWeek> daysOfWeek;
    private Integer priority;
    private Boolean active;
    private ContentStatusEnum status;
    private LocalDateTime createdAt;
}
