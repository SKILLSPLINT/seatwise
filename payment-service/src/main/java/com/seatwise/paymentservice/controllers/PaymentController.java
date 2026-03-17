package com.seatwise.paymentservice.controllers;

import com.seatwise.paymentservice.dtos.request.PaymentRequest;
import com.seatwise.paymentservice.dtos.response.PaymentResponse;
import com.seatwise.paymentservice.services.IPaymentService;
import dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    /**
     * POST /api/v1/payments/momo
     * Sends USSD prompt to the user's phone. They approve with MoMo PIN.
     * <p>
     * Body: { "phoneNumber": "0781234567", "amount": 1000,
     *         "orderReference": "ORDER-001", "description": "..." }
     */
    @PostMapping("/momo")
    public ResponseEntity<BaseResponse<PaymentResponse>> initiateMomoPayment(
            @Valid @RequestBody PaymentRequest request) {

        log.info("📲 MoMo payment request — phone: {}, amount: {}",
                request.getPhoneNumber(), request.getAmount());

        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(BaseResponse.success("payment initiated",response));
    }

    /**
     * GET /api/v1/payments/status/{ref}
     * Check status from your local database (fast).
     * Database is updated in real-time by Paypack webhook.
     */
    @GetMapping("/status/{transactionRef}")
    public ResponseEntity<PaymentResponse> checkStatus(
            @PathVariable String transactionRef) {

        PaymentResponse response = paymentService.checkStatus(transactionRef);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/payments/status/{ref}/live
     * Check status LIVE from Paypack API directly (slower but authoritative).
     * Use when a webhook wasn't received or for manual verification.
     */
    @GetMapping("/status/{transactionRef}/live")
    public ResponseEntity<PaymentResponse> checkStatusLive(
            @PathVariable String transactionRef) {

        PaymentResponse response = paymentService.checkStatusFromPaypack(transactionRef);
        return ResponseEntity.ok(response);
    }
}