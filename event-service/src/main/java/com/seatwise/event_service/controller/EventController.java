package com.seatwise.event_service.controller;

import com.seatwise.event_service.dto.request.CreateEventFormData;
import com.seatwise.event_service.dto.request.CreateEventRequest;
import com.seatwise.event_service.dto.request.UpdateEventRequest;
import com.seatwise.event_service.dto.response.EventDetailResponseDto;
import com.seatwise.event_service.dto.response.EventResponseDto;
import com.seatwise.event_service.dto.response.SeatResponseDto;
import com.seatwise.event_service.service.EventService;
import dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for event registration, updates, get all events, and event details")
public class EventController {
    private final EventService eventService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new event",
            description = "Creates a new event with seats and optional image. Send 'event' as JSON and 'image' as file.",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CreateEventFormData.class),
                            encoding = {
                                    @Encoding(name = "event", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            ),
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BaseResponse<EventResponseDto>> createEvent(
            @RequestPart("event") @Valid CreateEventRequest dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request
    ) {
        String userTimeZone = request.getHeader("time-zone");
        EventResponseDto eventResponse = eventService.createEvent(dto, image, userTimeZone);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success("Event created successfully", eventResponse)
        );
    }

    @PutMapping("/{eventId}")
    @Operation(
            summary = "Update an event",
            description = "Updates an existing event. If totalSeats is changed, seats will be recreated.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CreateEventFormData.class),
                            encoding = {
                                    @Encoding(name = "event", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            )

    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Event not found"),

    })
    public ResponseEntity<BaseResponse<EventResponseDto>> updateEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId,
            @RequestPart("event") @Valid UpdateEventRequest dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request
    ) {
        String userTimeZone = request.getHeader("time-zone");
        EventResponseDto eventResponse = eventService.updateEvent(eventId, dto, userTimeZone,image);
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

    @DeleteMapping("/{eventId}")
    @Operation(
            summary = "Delete event",
            description = "Deletes an event and all its related seats and image",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId
    ) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/reserve/{seatId}")
    @Operation(
            summary = "Reserve a seat",
            description = "Temporarily reserves a specific seat for the authenticated user. " +
                    "The reservation expires automatically if not confirmed within the allowed time window."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat reserved successfully"),
            @ApiResponse(responseCode = "400", description = "Seat is not available for reservation"),
            @ApiResponse(responseCode = "404", description = "Event or seat not found"),
            @ApiResponse(responseCode = "409", description = "Seat already reserved or booked")
    })
    public ResponseEntity<BaseResponse<SeatResponseDto>> reserveSeat(
            @Parameter(
                    description = "Authenticated user ID (passed from API Gateway)",
                    required = true,
                    example = "c1a2f3d4-5678-4abc-9def-1234567890ab"
            )
            @RequestHeader("X-User-Id") UUID userId,

            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @Parameter(
                    description = "Event ID",
                    required = true,
                    example = "9a1b2c3d-1111-4aaa-bbbb-ccccdddd0000"
            )
            @PathVariable UUID eventId,

            @Parameter(
                    description = "Seat ID to reserve",
                    required = true,
                    example = "7f8e9d10-2222-4bbb-aaaa-eeeeffff9999"
            )
            @PathVariable UUID seatId
    ) {
        SeatResponseDto seat = eventService.reserveSeat(seatId, userId, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Seat reserved successfully", seat)
        );
    }

    @PatchMapping("/{eventId}/confirm/{seatId}")
    @Operation(
            summary = "Confirm seat booking",
            description = "Finalizes a previously reserved seat and marks it as booked. " +
                    "Once confirmed, the seat cannot be released or reserved by other users."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Seat is not reserved or reservation expired"),
            @ApiResponse(responseCode = "404", description = "Event or seat not found"),
            @ApiResponse(responseCode = "409", description = "Seat already booked or confirmed by another user")
    })
    public ResponseEntity<BaseResponse<SeatResponseDto>> confirmSeat(
            @Parameter(
                    description = "Authenticated user ID (passed from API Gateway)",
                    required = true,
                    example = "c1a2f3d4-5678-4abc-9def-1234567890ab"
            )
            @RequestHeader("X-User-Id") UUID userId,

            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @Parameter(
                    description = "Event ID",
                    required = true,
                    example = "9a1b2c3d-1111-4aaa-bbbb-ccccdddd0000"
            )
            @PathVariable UUID eventId,

            @Parameter(
                    description = "Seat ID to confirm",
                    required = true,
                    example = "7f8e9d10-2222-4bbb-aaaa-eeeeffff9999"
            )
            @PathVariable UUID seatId
    ) {
        SeatResponseDto seat = eventService.confirmSeat(seatId, userId, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Seat confirmed successfully", seat)
        );
    }

}
