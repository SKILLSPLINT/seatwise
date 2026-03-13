package com.seatwise.paymentservice.dtos.response;// dto/paypack/PaypackAuthResponse.java

import lombok.Data;

@Data
public class PaypackAuthResponse {
    private String access;    // access token
    private String refresh;   // refresh token
    private String expires;   // expiry info
}