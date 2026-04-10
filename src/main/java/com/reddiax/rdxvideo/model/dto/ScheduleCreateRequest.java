package com.reddiax.rdxvideo.model.dto;

import com.reddiax.rdxvideo.constant.ContentStatusEnum;
import com.reddiax.rdxvideo.constant.ScheduleRepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {
    @NotBlank
    private String name;
    
    @NotNull
    private Long playlistId;
    
    @NotNull
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull
    private LocalTime startTime;
    
    @NotNull
    private LocalTime endTime;
    
    private ScheduleRepeatType repeatType;
    
    private Set<DayOfWeek> daysOfWeek;
    
    private Integer priority;
    
    private ContentStatusEnum status;
}
