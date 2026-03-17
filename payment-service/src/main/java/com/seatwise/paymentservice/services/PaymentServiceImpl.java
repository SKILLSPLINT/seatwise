package com.seatwise.paymentservice.services;

import com.seatwise.paymentservice.config.PaypackProperties;
import com.seatwise.paymentservice.dtos.request.PaymentRequest;
import com.seatwise.paymentservice.dtos.request.PaypackCashInRequest;
import com.seatwise.paymentservice.dtos.response.PaymentResponse;
import com.seatwise.paymentservice.dtos.response.PaypackTransactionResponse;
import com.seatwise.paymentservice.enums.EPaymentStatus;
import com.seatwise.paymentservice.models.Payment;
import com.seatwise.paymentservice.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository paymentRepo;
    private final PaypackTokenService tokenService;
    private final WebClient webClientConfig;
    private final PaypackProperties paypackProperties;

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating MoMo payment — Phone: {}, Amount: {} RWF",
                request.getPhoneNumber(), request.getAmount());

        if (paymentRepo.existsByOrderReference(request.getOrderReference())) {
            log.warn("Duplicate payment attempt for order: {}", request.getOrderReference());
            throw new IllegalStateException(
                    "Payment already initiated for order: " + request.getOrderReference());
        }

        String network = detectNetwork(request.getPhoneNumber());
        log.info("Detected network: {} for {}", network, request.getPhoneNumber());

        try {
            // ✅ Uses PaypackTransactionResponse — matches Paypack's exact JSON
            PaypackTransactionResponse paypackResponse = callPaypackCashin(request);

            log.info("✅ Paypack cashin — ref: {}, status: {}",
                    paypackResponse.getRef(), paypackResponse.getStatus());

            Payment transaction = Payment.builder()
                    .transactionRef(paypackResponse.getRef())
                    .orderReference(request.getOrderReference())
                    .phoneNumber(request.getPhoneNumber())
                    .amount(request.getAmount())
                    .status(EPaymentStatus.PENDING)
                    .network(network)
                    .userId(request.getUserID())
                    .userEmail(request.getUserEmail())
                    .bookingId(request.getBookingID())
                    .description(request.getDescription())
                    .build();

            paymentRepo.save(transaction);
            log.info("💾 Transaction saved to DB — ref: {}", paypackResponse.getRef());

            return PaymentResponse.builder()
                    .ref(paypackResponse.getRef())
                    .status("PENDING")
                    .message("✅ Payment request sent to " + request.getPhoneNumber()
                            + ". Please check your phone and enter your "
                            + network + " MoMo PIN to complete payment.")
                    .phoneNumber(request.getPhoneNumber())
                    .amount(request.getAmount())
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Paypack API error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401) {
                tokenService.invalidateToken();
                throw new RuntimeException("Authentication failed. Please try again.");
            }
            if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Too many requests. Please wait a moment and try again.");
            }
            throw new RuntimeException("Payment failed: " + e.getResponseBodyAsString());
        }
    }

    @Override
    public PaymentResponse checkStatus(String transactionRef) {
        Payment tx = paymentRepo
                .findByTransactionRef(transactionRef)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + transactionRef));

        return PaymentResponse.builder()
                .ref(tx.getTransactionRef())
                .status(tx.getStatus().name())
                .message(buildStatusMessage(tx))
                .phoneNumber(tx.getPhoneNumber())
                .amount(tx.getAmount())
                .build();
    }

    @Override
    public PaymentResponse checkStatusFromPaypack(String transactionRef) {
        log.info("Fetching live status from Paypack for ref: {}", transactionRef);

        try {
            String token = tokenService.getValidAccessToken();

            PaypackTransactionResponse response = webClientConfig.get()
                    .uri("/transactions/find/" + transactionRef)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(PaypackTransactionResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No response from Paypack for ref: " + transactionRef);
            }

            // DB is a source of truth — webhook keeps it updated in real-time
            Payment tx = paymentRepo.findByTransactionRef(transactionRef)
                    .orElseThrow(() -> new RuntimeException(
                            "Transaction not found: " + transactionRef));

            log.info("📡 Status check — ref: {}, status: {}", transactionRef, tx.getStatus());

            return PaymentResponse.builder()
                    .ref(response.getRef())
                    .status(tx.getStatus().name())
                    .phoneNumber(response.getClient())
                    .amount(response.getAmount())
                    .message(buildStatusMessage(tx))
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Error fetching live status: {}", e.getMessage());
            throw new RuntimeException("Could not fetch status: " + e.getMessage());
        }
    }

    private PaypackTransactionResponse callPaypackCashin(PaymentRequest request) {
        String token = tokenService.getValidAccessToken();

        return webClientConfig.post()
                .uri("/transactions/cashin")
                .header("Authorization", "Bearer " + token)
                .header("X-Webhook-Mode", paypackProperties.getWebhookMode())
                .bodyValue(new PaypackCashInRequest(
                        request.getAmount(),
                        request.getPhoneNumber()
                ))
                .retrieve()
                .bodyToMono(PaypackTransactionResponse.class) // ✅ correct DTO
                .block();
    }

    private String detectNetwork(String phone) {
        if (phone.startsWith("078") || phone.startsWith("079")) return "MTN";
        if (phone.startsWith("072") || phone.startsWith("073")) return "AIRTEL";
        return "UNKNOWN";
    }

    private String buildStatusMessage(Payment tx) {
        return switch (tx.getStatus()) {
            case PENDING -> "⏳ Waiting for " + tx.getPhoneNumber() + " to approve on phone.";
            case SUCCESS -> "✅ Payment of " + tx.getAmount() + " RWF received from " + tx.getPhoneNumber();
            case FAILED -> "❌ Payment failed: " +
                    (tx.getFailureReason() != null ? tx.getFailureReason() : "User declined or timeout");
        };
    }
}