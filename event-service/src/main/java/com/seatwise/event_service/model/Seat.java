package com.seatwise.event_service.model;

import enums.ESeat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"seat_number", "event_id"})
)
@Data
public class Seat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ESeat status = ESeat.AVAILABLE;

    @Column(name = "reserved_at", nullable = true)
    private Instant reservedAt;

    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, referencedColumnName = "id")
    private Event event;
    @Version
    private Long version;
}
