package com.seatwise.event_service.repository;

import com.seatwise.event_service.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
}
