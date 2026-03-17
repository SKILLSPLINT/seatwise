package com.seatwise.booking_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ConfirmBookingRequest {
    private  String phoneNumber;
    private UUID bookingId;
    private Double amount;
    private String orderReference;
    private String description;

}
