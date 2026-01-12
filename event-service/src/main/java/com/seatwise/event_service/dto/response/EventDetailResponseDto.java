package com.seatwise.event_service.dto.response;

import com.seatwise.event_service.model.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailResponseDto {
    private UUID id;
    private String name;
    private String title;
    private String description;
    private Venue venue;
    private String startTime; // formatted time in user timezone
    private Integer totalSeats;
    private Integer availableSeats;
    private String imageUrl; // URL to event image
    private List<SeatResponseDto> seats; // All seats with details
    private String createdAt; // formatted time in user timezone
    private String updatedAt; // formatted time in user timezone
}

