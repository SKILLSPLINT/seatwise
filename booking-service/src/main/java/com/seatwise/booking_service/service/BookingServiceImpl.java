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
        log.info("Starting atomic booking process for user: {} | Event: {} | Seat: {}", userId, request.getEventId(), request.getSeatId());

        // 1. Check if booking already exists (Idempotency check)
        bookingRepository.findByEventIdAndSeatId(request.getEventId(), request.getSeatId())
                .ifPresent(existing -> {
                    log.warn("Booking already exists for seat {} in event {}", request.getSeatId(), request.getEventId());
                    throw new ConflictException("Booking already exists for this seat, please proceed to payment");
                });

        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(request.getEventId())
                .seatId(request.getSeatId())
                .status(EBookingStatus.RESERVED)
                .reservedAt(Instant.now())
                .build();

        boolean seatReserved = false;
        Booking savedBooking = null;

        try {
            // 2. Reserve Seat in External Service
            // This is the first step in our Saga. If it fails, we haven't changed the local state yet.
            log.debug("Step 1: Reserving seat {} via EventService", request.getSeatId());
            SeatResponse seatResponse = eventService.reserveSeat(request.getSeatId(), request.getEventId(), userId, userEmail);
            seatReserved = true;

            // 3. Save Booking in Local Database
            // If this fails, we must unreserve the seat.
            log.debug("Step 2: Saving booking record to local database");
            savedBooking = bookingRepository.save(booking);

            // 4. Send Reservation Email (Async/Best-effort in Saga context)
            // If email fails, we might still proceed or choose to roll back.
            // Here we treat it as part of the transaction for maximum consistency.
            log.debug("Step 3: Sending reservation email");
            sendReservationEmail(savedBooking, seatResponse.getUserEmail(), userId);

            // 5. Initiate Payment
            // This is the most likely step to fail (network, external API).
            // If it fails, we MUST roll back: delete booking and unreserve seats.
            log.debug("Step 4: Initiating payment via PaymentService");
            paymentService.initiatePayment(
                    request.getPhoneNumber(),
                    savedBooking.getId(),
                    request.getAmount(),
                    userId,
                    userEmail,
                    request.getOrderReference(),
                    request.getDescription()
            );

            log.info("Booking process completed successfully for ID: {}", savedBooking.getId());
            return mapToDto(savedBooking, userTimeZone);

        } catch (Exception e) {
            log.error("Failure in booking process for user {}. Initiating compensation/rollback. Error: {}", userId, e.getMessage());
            rollbackBooking(seatReserved, request.getSeatId(), userId, userEmail, savedBooking);
            
            if (e instanceof ConflictException || e instanceof BadRequestException) {
                throw e;
            }
            throw new BadRequestException("Booking failed: " + e.getMessage());
        }
    }

    /**
     * Compensating transaction logic to ensure atomicity.
     */
    private void rollbackBooking(boolean seatReserved, UUID seatId, UUID userId, String userEmail, Booking savedBooking) {
        log.info("Rolling back partial booking state for seat: {}", seatId);
        
        // Order of rollback is usually the reverse of execution
        
        // 1. Delete local booking if it was saved
        if (savedBooking != null && savedBooking.getId() != null) {
            try {
                log.debug("Rollback: Deleting local booking record {}", savedBooking.getId());
                bookingRepository.deleteById(savedBooking.getId());
            } catch (Exception e) {
                log.error("Critical error: Failed to delete local booking during rollback: {}", e.getMessage());
            }
        }

        // 2. Unreserved seat in external service
        if (seatReserved) {
            try {
                log.debug("Rollback: Releasing seat {} in external event-service", seatId);
                eventService.unreserveSeat(seatId, userId, userEmail);
            } catch (Exception e) {
                log.error("Critical error: Failed to unreserved seat during rollback: {}", e.getMessage());
                // This might lead to "hanging" reserved seats until the auto-release job picks them up.
            }
        }
    }

    private void sendReservationEmail(Booking savedBooking, String recipientEmail, UUID userId) {
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
                .recipient(recipientEmail)
                .build();
        emailProducer.sendEmailNotification(userId, emailPayload);
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

    @Override
    @Transactional
    public void deleteBookingBySeatId(UUID seatId) {
        log.info("Deleting booking for seat: {}", seatId);
        bookingRepository.deleteBySeatId(seatId);
        log.info("Booking for seat {} deleted successfully", seatId);
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
