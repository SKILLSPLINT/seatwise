package com.seatwise.booking_service.repository;

import com.seatwise.booking_service.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

    Optional<Booking> findByEventIdAndSeatId(UUID eventId, UUID seatId);

    Optional<Booking> findByIdAndUserId(UUID id, UUID userId);
}
