package com.reddiax.rdxvideo.controller;

import com.reddiax.rdxvideo.model.dto.ScheduleCreateRequest;
import com.reddiax.rdxvideo.model.dto.ScheduleDTO;
import com.reddiax.rdxvideo.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/schedules")
@Tag(name = "Schedule Management", description = "API for managing schedules")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "Get all schedules")
    public List<ScheduleDTO> getAllSchedules(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching all schedules", jwt.getSubject());
        return scheduleService.getAllSchedules();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get schedule by ID")
    public ScheduleDTO getSchedule(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching schedule: {}", jwt.getSubject(), id);
        return scheduleService.getSchedule(id);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current active schedule")
    public ScheduleDTO getCurrentSchedule(@AuthenticationPrincipal Jwt jwt) {
        log.debug("User {} fetching current active schedule", jwt.getSubject());
        return scheduleService.getCurrentActiveSchedule();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new schedule")
    public ScheduleDTO createSchedule(
            @Valid @RequestBody ScheduleCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} creating schedule: {}", jwt.getSubject(), request.getName());
        return scheduleService.createSchedule(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a schedule")
    public ScheduleDTO updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} updating schedule: {}", jwt.getSubject(), id);
        return scheduleService.updateSchedule(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a schedule")
    public void deleteSchedule(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} deleting schedule: {}", jwt.getSubject(), id);
        scheduleService.deleteSchedule(id);
    }

    @PostMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle schedule active status")
    public ScheduleDTO toggleActive(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        log.info("User {} toggling schedule active status: {}", jwt.getSubject(), id);
        return scheduleService.toggleActive(id);
    }
}
