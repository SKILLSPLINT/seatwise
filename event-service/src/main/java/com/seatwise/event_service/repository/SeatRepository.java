package com.seatwise.event_service.repository;

import com.seatwise.event_service.model.Seat;
import enums.ESeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    /**
     * Find all seats that are RESERVED and reserved before the given timestamp.
     *
     * @param status The seat status to filter (RESERVED)
     * @return List of reserved seats
     */
    List<Seat> findByStatus(ESeat status);
}
