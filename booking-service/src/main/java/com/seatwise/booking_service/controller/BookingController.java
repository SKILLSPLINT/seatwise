package com.seatwise.booking_service.controller;

import com.seatwise.booking_service.dto.request.CreateBookingRequest;
import com.seatwise.booking_service.dto.response.BookingResponseDto;
import com.seatwise.booking_service.service.BookingService;
import dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for creating, retrieving, and confirming bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
            summary = "Create a new booking",
            description = "Creates a new booking for the authenticated user. The booking starts in RESERVED status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "409", description = "Booking already exists for this seat")
    })
    public ResponseEntity<BaseResponse<BookingResponseDto>> createBooking(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") UUID userId,

            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @Valid @RequestBody CreateBookingRequest request
    ) {
        BookingResponseDto booking = bookingService.createBooking(request, userId, userTimeZone);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success("Booking created successfully", booking)
        );
    }

    @GetMapping
    @Operation(
            summary = "Get all bookings (Admin only)",
            description = "Retrieves a paginated list of all bookings in the system. Only accessible by administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin role required")
    })
    public ResponseEntity<BaseResponse<Page<BookingResponseDto>>> getAllBookings(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String userRole,

            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<BookingResponseDto> bookings = bookingService.getAllBookings(pageable, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Bookings retrieved successfully", bookings)
        );
    }

    @GetMapping("/my-bookings")
    @Operation(
            summary = "Get bookings for authenticated user",
            description = "Retrieves a paginated list of bookings for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User bookings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required")
    })
    public ResponseEntity<BaseResponse<Page<BookingResponseDto>>> getMyBookings(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") UUID userId,

            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<BookingResponseDto> bookings = bookingService.getBookingsByUserId(userId, pageable, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("User bookings retrieved successfully", bookings)
        );
    }

    @GetMapping("/{bookingId}")
    @Operation(
            summary = "Get booking by ID",
            description = "Retrieves detailed information about a specific booking.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BaseResponse<BookingResponseDto>> getBookingById(
            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @Parameter(description = "Booking ID", required = true)
            @PathVariable UUID bookingId
    ) {
        BookingResponseDto booking = bookingService.getBookingById(bookingId, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Booking details retrieved successfully", booking)
        );
    }

    @PatchMapping("/{bookingId}/confirm")
    @Operation(
            summary = "Confirm a booking",
            description = "Confirms a previously reserved booking and marks it as BOOKED. " +
                    "Only the user who created the booking can confirm it.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be confirmed (invalid status)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to confirm this booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BaseResponse<BookingResponseDto>> confirmBooking(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") UUID userId,

            @Parameter(
                    description = "User time zone (e.g., Africa/Kigali, Europe/London)",
                    example = "Africa/Kigali"
            )
            @RequestHeader(value = "time-zone", required = false) String userTimeZone,

            @Parameter(description = "Booking ID to confirm", required = true)
            @PathVariable UUID bookingId
    ) {
        BookingResponseDto booking = bookingService.confirmBooking(bookingId, userId, userTimeZone);
        return ResponseEntity.ok(
                BaseResponse.success("Booking confirmed successfully", booking)
        );
    }
}
