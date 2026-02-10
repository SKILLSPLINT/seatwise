package com.seatwise.booking_service.dto.response;

import enums.EBookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Booking response containing booking details")
public class BookingResponseDto {

    @Schema(description = "Unique booking ID", example = "a1b2c3d4-5678-4abc-9def-1234567890ab")
    private UUID id;

    @Schema(description = "ID of the user who made the booking", example = "c1a2f3d4-5678-4abc-9def-1234567890ab")
    private UUID userId;

    @Schema(description = "ID of the booked event", example = "9a1b2c3d-1111-4aaa-bbbb-ccccdddd0000")
    private UUID eventId;

    @Schema(description = "ID of the booked seat", example = "7f8e9d10-2222-4bbb-aaaa-eeeeffff9999")
    private UUID seatId;

    @Schema(description = "Current booking status", example = "RESERVED")
    private EBookingStatus status;

    @Schema(description = "Timestamp when the seat was reserved", example = "2024-01-15 14:30:00")
    private String reservedAt;

    @Schema(description = "Timestamp when the booking was paid/confirmed", example = "2024-01-15 14:35:00")
    private String paidAt;

    @Schema(description = "Timestamp when the booking was created", example = "2024-01-15 14:30:00")
    private String createdAt;

    @Schema(description = "Timestamp when the booking was last updated", example = "2024-01-15 14:35:00")
    private String updatedAt;
}
