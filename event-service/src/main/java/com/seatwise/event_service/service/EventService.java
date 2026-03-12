package com.seatwise.event_service.service;

import com.seatwise.event_service.dto.request.CreateEventRequest;
import com.seatwise.event_service.dto.request.UpdateEventRequest;
import com.seatwise.event_service.dto.response.EventDetailResponseDto;
import com.seatwise.event_service.dto.response.EventResponseDto;
import com.seatwise.event_service.dto.response.SeatResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

public interface EventService {
    EventResponseDto createEvent(CreateEventRequest dto, MultipartFile image, String userTimeZone);

    EventResponseDto updateEvent(UUID eventId, UpdateEventRequest dto, String userTimeZone, MultipartFile image);

    Page<EventResponseDto> getAllEvents(Pageable pageable, String userTimeZone);

    EventDetailResponseDto getEventById(UUID eventId, String userTimeZone);

    void deleteEvent(UUID eventId);

    SeatResponseDto reserveSeat(UUID seatId, UUID userId, String userTimeZone);

    SeatResponseDto confirmSeat(UUID seatId,UUID userId, String userTimeZone);

    Page<EventResponseDto> search(String query, String venueName, Instant fromTime, Instant toTime, Integer minAvailableSeats, Pageable pageable, String userTimeZone);
}
