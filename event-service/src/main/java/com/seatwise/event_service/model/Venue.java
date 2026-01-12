package com.seatwise.event_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Venue {
    private String name;        // e.g., ORAD Hall
    private String address;     // e.g., 123 Main Street
    private String city;        // Kigali
    private String country;     // Rwanda
    private Double latitude;    // optional GPS
    private Double longitude;   // optional GPS
}
