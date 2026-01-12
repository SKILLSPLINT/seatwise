package com.seatwise.event_service.service;

import com.seatwise.event_service.dto.request.CreateEventRequest;
import com.seatwise.event_service.dto.request.UpdateEventRequest;
import com.seatwise.event_service.dto.response.EventDetailResponseDto;
import com.seatwise.event_service.dto.response.EventResponseDto;
import com.seatwise.event_service.dto.response.SeatResponseDto;
import com.seatwise.event_service.model.Event;
import com.seatwise.event_service.model.Seat;
import com.seatwise.event_service.repository.EventRepository;
import enums.ESeat;
import exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utils.TimeUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepo;

    @Override
    @Transactional
    public EventResponseDto createEvent(CreateEventRequest dto, String userTimeZone) {
        log.info("Creating new event: {}", dto.getName());

        Event event = buildEventFromDto(dto, userTimeZone, null);

        // Create seats and add to event (cascade will save them)
        createSeatsForEvent(event, dto.getTotalSeats());

        Event savedEvent = eventRepo.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());

        // Fetch with relationships to avoid LazyInitializationException
        return mapToEventResponseDto(savedEvent, userTimeZone);
    }

    @Override
    @Transactional
    public EventResponseDto updateEvent(UUID eventId, UpdateEventRequest dto, String userTimeZone) {
        log.info("Updating event with ID: {}", eventId);

        Event existingEvent = eventRepo.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event not found with ID: {}", eventId);
                    return new ResourceNotFoundException("Event", "id", eventId.toString());
                });

        // If totalSeats changed, recreate seats
        if (dto.getTotalSeats() != null && !dto.getTotalSeats().equals(existingEvent.getTotalSeats())) {
            log.info("Total seats changed from {} to {}, recreating seats",
                    existingEvent.getTotalSeats(), dto.getTotalSeats());
            // Delete old seats (cascade will handle this with orphanRemoval)
            existingEvent.getSeats().clear();
            // Create new seats
            createSeatsForEvent(existingEvent, dto.getTotalSeats());
            existingEvent.setAvailableSeats(dto.getTotalSeats());
        }

        // Update event fields using reusable method
        updateEventFromDto(existingEvent, dto, userTimeZone);

        Event updatedEvent = eventRepo.save(existingEvent);
        log.info("Event updated successfully with ID: {}", updatedEvent.getId());

        return mapToEventResponseDto(updatedEvent, userTimeZone);
    }

    @Override
    @Transactional
    public Page<EventResponseDto> getAllEvents(Pageable pageable, String userTimeZone) {
        log.info("Fetching all events - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Event> eventsPage = eventRepo.findAll(pageable);

        // Map events to DTOs (without seats)
        Page<EventResponseDto> eventResponsePage = eventsPage.map(event -> mapToEventResponseDto(event, userTimeZone));

        log.info("Retrieved {} events out of {} total", eventResponsePage.getNumberOfElements(),
                eventResponsePage.getTotalElements());
        return eventResponsePage;
    }

    @Override
    @Transactional
    public EventDetailResponseDto getEventById(UUID eventId, String userTimeZone) {
        log.info("Fetching event details for ID: {}", eventId);

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event not found with ID: {}", eventId);
                    return new ResourceNotFoundException("Event", "id", eventId.toString());
                });

        // Access seats to initialize the collection (prevent
        // LazyInitializationException)
        Set<Seat> seats = event.getSeats();
        log.debug("Event {} has {} seats", eventId, seats != null ? seats.size() : 0);

        return mapToEventDetailResponseDto(event, userTimeZone);
    }

    /**
     * Reusable method to build Event entity from DTO (for creation)
     */
    private Event buildEventFromDto(CreateEventRequest dto, String userTimeZone, Event existingEvent) {
        Event event = existingEvent != null ? existingEvent : new Event();

        event.setName(dto.getName());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setVenue(dto.getVenue());
        event.setTotalSeats(dto.getTotalSeats());
        event.setAvailableSeats(dto.getTotalSeats());

        // Convert local datetime to Instant (UTC)
        ZoneId zone = ZoneId.of(userTimeZone != null && !userTimeZone.isBlank()
                ? userTimeZone
                : "UTC");
        LocalDateTime localDateTime = LocalDateTime.of(dto.getLocalDate(), dto.getLocalTime());
        Instant startTime = localDateTime.atZone(zone).toInstant();
        event.setTime(startTime);

        return event;
    }

    /**
     * Reusable method to update Event entity from DTO (for update)
     */
    private void updateEventFromDto(Event event, UpdateEventRequest dto, String userTimeZone) {
        if (dto.getName() != null) {
            event.setName(dto.getName());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getVenue() != null) {
            event.setVenue(dto.getVenue());
        }
        if (dto.getTotalSeats() != null) {
            event.setTotalSeats(dto.getTotalSeats());
        }

        // Update time if provided
        if (dto.getLocalDate() != null && dto.getLocalTime() != null) {
            ZoneId zone = ZoneId.of(userTimeZone != null && !userTimeZone.isBlank()
                    ? userTimeZone
                    : "UTC");
            LocalDateTime localDateTime = LocalDateTime.of(dto.getLocalDate(), dto.getLocalTime());
            Instant startTime = localDateTime.atZone(zone).toInstant();
            event.setTime(startTime);
        }
    }

    /**
     * Creates seats for an event and adds them to the event's seats collection
     */
    private void createSeatsForEvent(Event event, int totalSeats) {
        Set<Seat> seats = new HashSet<>();
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setSeatNumber("S" + String.format("%04d", i)); // Format: S0001, S0002, etc.
            seat.setEvent(event);
            seat.setStatus(ESeat.AVAILABLE);
            seats.add(seat);
        }
        event.getSeats().addAll(seats);
    }

    /**
     * Maps Event entity to EventResponseDto, handling LazyInitializationException
     * by accessing relationships within the transactional context
     */
    private EventResponseDto mapToEventResponseDto(Event event, String userTimeZone) {
        // Access lazy-loaded relationships within transaction to initialize them
        // This prevents LazyInitializationException when DTO is returned
        // The seats collection is accessed via event.getSeats() to trigger
        // initialization

        String imageUrl = null;
        if (event.getImage() != null) {
            // Build URL for event image
            imageUrl = "/api/v1/files/" + event.getImage().getId();
        }

        return EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startTime(TimeUtils.toUserOrUtc(event.getTime(), userTimeZone))
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .imageUrl(imageUrl)
                .createdAt(TimeUtils.toUserOrUtc(event.getCreatedAt(), userTimeZone))
                .updatedAt(TimeUtils.toUserOrUtc(event.getUpdatedAt(), userTimeZone))
                .build();
    }

    /**
     * Maps Event entity to EventDetailResponseDto with seats included
     */
    private EventDetailResponseDto mapToEventDetailResponseDto(Event event, String userTimeZone) {
        String imageUrl = null;
        if (event.getImage() != null) {
            imageUrl = "/api/v1/files/" + event.getImage().getId();
        }

        // Map seats to SeatResponseDto
        List<SeatResponseDto> seatDtos = event.getSeats().stream()
                .sorted(Comparator.comparing(Seat::getSeatNumber))
                .map(seat -> SeatResponseDto.builder()
                        .id(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .status(seat.getStatus())
                        .reservedAt(TimeUtils.toUserOrUtc(seat.getReservedAt(), userTimeZone))
                        .userId(seat.getUserId())
                        .createdAt(TimeUtils.toUserOrUtc(seat.getCreatedAt(), userTimeZone))
                        .updatedAt(TimeUtils.toUserOrUtc(seat.getUpdatedAt(), userTimeZone))
                        .build())
                .collect(Collectors.toList());
        return EventDetailResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startTime(TimeUtils.toUserOrUtc(event.getTime(), userTimeZone))
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .imageUrl(imageUrl)
                .seats(seatDtos)
                .createdAt(TimeUtils.toUserOrUtc(event.getCreatedAt(), userTimeZone))
                .updatedAt(TimeUtils.toUserOrUtc(event.getUpdatedAt(), userTimeZone))
                .build();
    }

    /**
     * Maps Seat entity to SeatResponseDto
     */
    private SeatResponseDto mapToSeatResponseDto(Seat seat, String userTimeZone) {
        return SeatResponseDto.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .reservedAt(TimeUtils.toUserOrUtc(seat.getReservedAt(), userTimeZone))
                .userId(seat.getUserId())
                .createdAt(TimeUtils.toUserOrUtc(seat.getCreatedAt(), userTimeZone))
                .updatedAt(TimeUtils.toUserOrUtc(seat.getUpdatedAt(), userTimeZone))
                .build();
    }
}
