package com.seatwise.event_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "events")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)

public class Event extends BaseEntity {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "venue_name")),
            @AttributeOverride(name = "address", column = @Column(name = "venue_address"))
    })
    private Venue venue;

    @Column(name = "start_time", nullable = false)
    private Instant time;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Seat> seats = new HashSet<>();


    @JoinColumn(name = "image_id")
    @OneToOne(cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File image;

}
