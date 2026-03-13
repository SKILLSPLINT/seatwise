package com.seatwise.booking_service.dto.response;

import enums.ESeat;
import lombok.Data;

import java.util.UUID;
@Data
public class SeatResponse {
    private UUID id;
    private String seatNumber;
    private ESeat status;
    private String reservedAt; // formatted time in user timezone
    private UUID userId;
    private String userEmail;
    private String createdAt; // formatted time in user timezone
    private String updatedAt; // formatted time in user timezone
}
