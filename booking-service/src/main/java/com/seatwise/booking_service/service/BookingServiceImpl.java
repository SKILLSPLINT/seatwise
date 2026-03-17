package com.seatwise.booking_service.service;

import com.seatwise.booking_service.dto.request.CreateBookingRequest;
import com.seatwise.booking_service.dto.response.BookingResponseDto;
import com.seatwise.booking_service.dto.response.SeatResponse;
import com.seatwise.booking_service.model.Booking;
import com.seatwise.booking_service.repository.BookingRepository;
import dto.EmailPayload;
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
    private final EventService eventService;
    private final EmailProducer emailProducer;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public BookingResponseDto createBooking(CreateBookingRequest request, UUID userId, String userEmail, String userTimeZone) {
        log.info("Creating booking for user: {} for event: {} seat: {}", userId, request.getEventId(), request.getSeatId());

        // Check if booking already exists for this event and seat
        bookingRepository.findByEventIdAndSeatId(request.getEventId(), request.getSeatId())
                .ifPresent(existing -> {
                    throw new ConflictException("Booking already exists for this seat , move to payment");
                });

        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(request.getEventId())
                .seatId(request.getSeatId())
                .status(EBookingStatus.RESERVED)
                .reservedAt(Instant.now())
                .build();

        SeatResponse seatResponse = eventService.reserveSeat(request.getSeatId(), request.getEventId(), userId, userEmail);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getId());

        EmailPayload emailPayload = EmailPayload.builder()
                .subject("Seat Reserved")
                .body("Hello,\n\n" +
                        "Your seat has been successfully reserved.\n\n" +
                        "Booking ID: " + savedBooking.getId() + "\n" +
                        "Event ID: " + savedBooking.getEventId() + "\n" +
                        "Seat ID: " + savedBooking.getSeatId() + "\n\n" +
                        "Please complete your payment before the reservation expires.\n\n" +
                        "Thank you for using Seatwise.\n" +
                        "Seatwise Team")
                .sender("info@seatwise.dpdns.org")
                .recipient(seatResponse.getUserEmail())
                .build();
        emailProducer.sendEmailNotification(seatResponse.getUserId(), emailPayload);
        paymentService.initiatePayment(request.getPhoneNumber(),savedBooking.getId(),request.getAmount(),userId, userEmail, request.getOrderReference(),request.getDescription());

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
    public BookingResponseDto confirmBooking(UUID bookingId, UUID userID, String userEmail, String userTimeZone) {
        log.info("Confirming booking: {} for user: {}", bookingId, userID);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Ensure the user owns this booking
        if (!booking.getUserId().equals(userID)) {
            throw new UnauthorizedException("You are not authorized to confirm this booking");
        }

        // Check if booking is in RESERVED status
        if (booking.getStatus() == EBookingStatus.BOOKED) {
            log.info("Booking {} already confirmed, skipping.", bookingId);
            return mapToDto(booking, userTimeZone);
        }

        if (booking.getStatus() != EBookingStatus.RESERVED) {
            throw new BadRequestException("Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        booking.setStatus(EBookingStatus.BOOKED);
        booking.setPaidAt(Instant.now());
        SeatResponse seatResponse = eventService.confirmSeat(booking.getSeatId(), userID, userEmail);

        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed successfully: {}",bookingId);
        EmailPayload emailPayload = EmailPayload.builder()
                .subject("Seat Confirmed and Payment Completed")
                .body("Hello " + seatResponse.getUserEmail() + ",\n\n" +
                        "Your booking has been successfully confirmed and payment has been completed.\n\n" +
                        "Booking Details:\n" +
                        "Booking ID: " + confirmedBooking.getId() + "\n" +
                        "Event ID: " + confirmedBooking.getEventId() + "\n" +
                        "Seat ID: " + confirmedBooking.getSeatId() + "\n\n" +
                        "Thank you for using SeatWise! We look forward to seeing you at the event.\n\n" +
                        "Best regards,\n" +
                        "SeatWise Team")
                .sender("info@seatwise.dpdns.org")
                .recipient(seatResponse.getUserEmail())
                .build();
        emailProducer.sendEmailNotification(seatResponse.getUserId(), emailPayload);
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
