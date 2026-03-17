package com.seatwise.booking_service.service;

import com.seatwise.booking_service.client.PaymentClient;
import com.seatwise.booking_service.dto.response.PaymentResponse;
import exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentService {
    private final PaymentClient client;

    PaymentResponse initiatePayment(String phoneNumber, UUID bookingId, Double amount, UUID userId, String orderReference, String description) {
        try {
            return client.initiatePayment(phoneNumber, bookingId, amount, userId, orderReference, description);
        } catch (Exception e) {
            log.error("Error initiating payment", e);
            throw new BadRequestException("To initiate payment failed , please try again");
        }
    }
}
