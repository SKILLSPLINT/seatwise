package com.seatwise.event_service.dto.response;

import enums.ESeat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponseDto {
    private UUID id;
    private String seatNumber;
    private ESeat status;
    private String reservedAt; // formatted time in user timezone
    private UUID userId;
    private String createdAt; // formatted time in user timezone
    private String updatedAt; // formatted time in user timezone
}

