package com.seatwise.event_service.controller;

import com.seatwise.event_service.dto.request.CreateEventRequest;
import com.seatwise.event_service.dto.request.UpdateEventRequest;
import com.seatwise.event_service.dto.response.EventDetailResponseDto;
import com.seatwise.event_service.dto.response.EventResponseDto;
import com.seatwise.event_service.service.EventService;
import dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for event registration, updates, get all events, and event details")
public class EventController {
    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event with seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BaseResponse<EventResponseDto>> createEvent(
            @Valid @RequestBody CreateEventRequest dto, HttpServletRequest request
    ) {
        String userTimeZone = request.getHeader("time-zone");
        EventResponseDto eventResponse = eventService.createEvent(dto, userTimeZone);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success("Event created successfully", eventResponse)
        );
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update an event", description = "Updates an existing event. If totalSeats is changed, seats will be recreated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<BaseResponse<EventResponseDto>> updateEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest dto,
            HttpServletRequest request
    ) {
        String userTimeZone = request.getHeader("time-zone");
        EventResponseDto eventResponse = eventService.updateEvent(eventId, dto, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Event updated successfully", eventResponse)
        );
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves a paginated list of all events (seats not included)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    })
    public ResponseEntity<Page<EventResponseDto>> getAllEvents(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            HttpServletRequest request
    ) {
        String userTimeZone = request.getHeader("time-zone");
        Page<EventResponseDto> eventsPage = eventService.getAllEvents(pageable, userTimeZone);
        return ResponseEntity.ok(eventsPage);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID", description = "Retrieves detailed information about a specific event including all seats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<BaseResponse<EventDetailResponseDto>> getEventById(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId,
            HttpServletRequest request
    ) {
        String userTimeZone = request.getHeader("time-zone");
        EventDetailResponseDto eventDetail = eventService.getEventById(eventId, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Event details retrieved successfully", eventDetail)
        );
    }
}
