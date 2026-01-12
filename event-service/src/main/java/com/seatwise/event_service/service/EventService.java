package com.seatwise.event_service.service;

import com.seatwise.event_service.dto.request.CreateEventRequest;
import com.seatwise.event_service.dto.request.UpdateEventRequest;
import com.seatwise.event_service.dto.response.EventDetailResponseDto;
import com.seatwise.event_service.dto.response.EventResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {
    EventResponseDto createEvent(CreateEventRequest dto, String userTimeZone);
    EventResponseDto updateEvent(UUID eventId, UpdateEventRequest dto, String userTimeZone);
    Page<EventResponseDto> getAllEvents(Pageable pageable, String userTimeZone);
    EventDetailResponseDto getEventById(UUID eventId, String userTimeZone);
}
