package com.seatwise.booking_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(07[2-9]\\d{7})$",
            message = "Enter valid Rwanda number e.g. 0781234567"
    )
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @Min(value = 50, message = "Minimum amount is 50 RWF")
    private Double amount;

    private UUID bookingID;
    private UUID userID;
    private String userEmail;

    @NotBlank(message = "Order reference is required")
    private String orderReference;

    private String description;

}