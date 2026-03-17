package com.seatwise.paymentservice.dtos.request;// dto/request/PaymentRequest.java  — what your client (frontend/app) sends YOU

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
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