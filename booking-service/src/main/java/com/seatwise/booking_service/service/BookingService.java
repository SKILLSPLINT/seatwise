package com.seatwise.booking_service.service;

import com.seatwise.booking_service.dto.request.CreateBookingRequest;
import com.seatwise.booking_service.dto.response.BookingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {

    BookingResponseDto createBooking(CreateBookingRequest request, UUID userId,String userEmail, String userTimeZone);

    Page<BookingResponseDto> getAllBookings(Pageable pageable, String userTimeZone);

    Page<BookingResponseDto> getBookingsByUserId(UUID userId, Pageable pageable, String userTimeZone);

    BookingResponseDto confirmBooking(UUID bookingId, UUID userId,String userEmail, String userTimeZone);

    BookingResponseDto getBookingById(UUID bookingId, String userTimeZone);
}
