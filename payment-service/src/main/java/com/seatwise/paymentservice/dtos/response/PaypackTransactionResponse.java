package com.seatwise.paymentservice.dtos.response;// dtos/paypack/PaypackTransactionResponse.java

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaypackTransactionResponse {
    private String ref;
    private Double amount;
    private String status;   // "pending", "successful", "failed"
    private String kind;     // "CASHIN"
    private String client;   // ← Paypack's field for phone number
    private String merchant;
    private Double fee;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("processed_at")
    private String processedAt;
}