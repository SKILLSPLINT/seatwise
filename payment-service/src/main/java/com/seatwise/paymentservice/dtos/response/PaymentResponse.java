package com.seatwise.paymentservice.dtos.response;// dto/response/PaymentResponse.java

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    @JsonProperty("ref")
    private String ref;   // Paypack ref — client polls with this
    private String status;           // PENDING / SUCCESS / FAILED
    private String message;          // User-friendly message
    private String phoneNumber;
    private Double amount;
}