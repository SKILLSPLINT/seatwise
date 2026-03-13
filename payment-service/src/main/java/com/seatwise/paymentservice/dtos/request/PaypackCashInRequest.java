package com.seatwise.paymentservice.dtos.request;// dto/paypack/PaypackCashinRequest.java

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaypackCashInRequest {
    private Double amount;
    private String number; // phone number e.g. 078xxxxxxx
}