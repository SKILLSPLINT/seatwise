package com.seatwise.booking_service.model;

import enums.EBookingStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "bookings")
public class Booking extends BaseEntity {
    private UUID userId;
    private UUID eventId;
    private UUID seatId;
    private EBookingStatus status;
    private Instant reservedAt;
    private Instant paidAt;
}