package com.seatwise.paymentservice.services;

import com.seatwise.paymentservice.dtos.request.PaymentRequest;
import com.seatwise.paymentservice.dtos.response.PaymentResponse;

public interface IPaymentService {

    /**
     * Initiates a MoMo payment request.
     * Sends a USSD prompt to the user's phone.
     */
    PaymentResponse initiatePayment(PaymentRequest request);

    /**
     * Check transaction status stored in the database.
     */
    PaymentResponse checkStatus(String transactionRef);

    /**
     * Fetch real-time transaction status directly from Paypack API.
     */
    PaymentResponse checkStatusFromPaypack(String transactionRef);
}