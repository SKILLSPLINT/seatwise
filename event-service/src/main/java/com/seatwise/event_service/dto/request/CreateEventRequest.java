package com.seatwise.event_service.dto.request;

import com.seatwise.event_service.model.Venue;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateEventRequest {
    private String name;
    private String title;
    private String description;
    private Venue venue;
    private LocalDate localDate;
    private LocalTime localTime;
    private Integer totalSeats;
}
