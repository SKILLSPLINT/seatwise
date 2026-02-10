package com.seatwise.booking_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new booking")
public class CreateBookingRequest {

    @NotNull(message = "Event ID is required")
    @Schema(description = "ID of the event to book", example = "9a1b2c3d-1111-4aaa-bbbb-ccccdddd0000", required = true)
    private UUID eventId;

    @NotNull(message = "Seat ID is required")
    @Schema(description = "ID of the seat to book", example = "7f8e9d10-2222-4bbb-aaaa-eeeeffff9999", required = true)
    private UUID seatId;
}
