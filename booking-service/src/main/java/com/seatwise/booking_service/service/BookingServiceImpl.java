package com.seatwise.booking_service.service;

import com.seatwise.booking_service.dto.request.CreateBookingRequest;
import com.seatwise.booking_service.dto.response.BookingResponseDto;
import com.seatwise.booking_service.model.Booking;
import com.seatwise.booking_service.repository.BookingRepository;
import enums.EBookingStatus;
import exception.BadRequestException;
import exception.ConflictException;
import exception.ResourceNotFoundException;
import exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utils.TimeUtils;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(CreateBookingRequest request, UUID userId, String userTimeZone) {
        log.info("Creating booking for user: {} for event: {} seat: {}", userId, request.getEventId(), request.getSeatId());

        // Check if booking already exists for this event and seat
        bookingRepository.findByEventIdAndSeatId(request.getEventId(), request.getSeatId())
                .ifPresent(existing -> {
                    throw new ConflictException("Booking already exists for this seat");
                });

        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(request.getEventId())
                .seatId(request.getSeatId())
                .status(EBookingStatus.RESERVED)
                .reservedAt(Instant.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getId());

        return mapToDto(savedBooking, userTimeZone);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getAllBookings(Pageable pageable, String userTimeZone) {
        log.info("Fetching all bookings (admin)");
        return bookingRepository.findAll(pageable).map(booking -> mapToDto(booking, userTimeZone));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> getBookingsByUserId(UUID userId, Pageable pageable, String userTimeZone) {
        log.info("Fetching bookings for user: {}", userId);
        return bookingRepository.findByUserId(userId, pageable).map(booking -> mapToDto(booking, userTimeZone));
    }

    @Override
    @Transactional
//    this must be called by payment service
    public BookingResponseDto confirmBooking(UUID bookingId, UUID userId, String userTimeZone) {
        log.info("Confirming booking: {} for user: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Ensure the user owns this booking
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to confirm this booking");
        }

        // Check if booking is in RESERVED status
        if (booking.getStatus() != EBookingStatus.RESERVED) {
            throw new BadRequestException("Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        booking.setStatus(EBookingStatus.BOOKED);
        booking.setPaidAt(Instant.now());

        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed successfully: {}", bookingId);

        return mapToDto(confirmedBooking, userTimeZone);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(UUID bookingId, String userTimeZone) {
        log.info("Fetching booking by ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        return mapToDto(booking, userTimeZone);
    }

    private BookingResponseDto mapToDto(Booking booking, String userTimeZone) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .eventId(booking.getEventId())
                .seatId(booking.getSeatId())
                .status(booking.getStatus())
                .reservedAt(TimeUtils.toUserOrUtc(booking.getReservedAt(), userTimeZone))
                .paidAt(TimeUtils.toUserOrUtc(booking.getPaidAt(), userTimeZone))
                .createdAt(TimeUtils.toUserOrUtc(booking.getCreatedAt(), userTimeZone))
                .updatedAt(TimeUtils.toUserOrUtc(booking.getUpdatedAt(), userTimeZone))
                .build();
    }
}
