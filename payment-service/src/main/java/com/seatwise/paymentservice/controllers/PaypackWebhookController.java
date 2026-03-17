package com.seatwise.paymentservice.controllers;
//TODO:do cashout
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatwise.paymentservice.config.PaypackProperties;
import com.seatwise.paymentservice.enums.EPaymentStatus;
import com.seatwise.paymentservice.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class PaypackWebhookController {

    private final IPaymentRepository transactionRepository;
    private final PaypackProperties paypackProperties;
    private final ObjectMapper objectMapper;

    /**
     * Paypack POSTs here when a transaction is processed (success or failure).
     * <p>
     * Register this URL in your Paypack dashboard:
     *   Production: <a href="https://yourdomain.com/api/v1/webhooks/paypack">...</a>
     *   Local dev: use localhost. Run (see STEP 14)
     * <p>
     * Payload structure:
     * {
     *   "event_id": "...",
     *   "kind": "transaction:processed",
     *   "created_at": "...",
     *   "data": {
     *     "ref": "...",
     *     "kind": "CASHIN",
     *     "status": "successful" | "failed",
     *     "amount": 1000,
     *     "client": "078xxxxxxx",
     *     "provider": "mtn",
     *     "fee": 2.3,
     *     ...
     *   }
     * }
     */
    @PostMapping("/paypack")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Paypack-Signature", required = false) String signature) {

        log.info("📩 Webhook received from Paypack");

        // Step 1: Verify signature (security — ensures it's really Paypack)
        if (signature != null && !verifySignature(rawBody, signature)) {
            log.warn("❌ Invalid webhook signature — rejected");
            return ResponseEntity.status(401).build();
        }

        try {
            JsonNode payload = objectMapper.readTree(rawBody);
            String eventKind = payload.path("kind").asText();

            log.info("Event kind: {}", eventKind);

            if (!"transaction:processed".equals(eventKind)) {
                log.info("Ignoring non-transaction event: {}", eventKind);
                return ResponseEntity.ok().build();
            }

            // Extract transaction data from a nested "data" object
            JsonNode data = payload.path("data");
            String transactionRef = data.path("ref").asText();
            String status = data.path("status").asText();       // "successful" or "failed"
            String provider = data.path("provider").asText();   // "mtn" or "airtel"
            double amount = data.path("amount").asDouble();
            double fee = data.path("fee").asDouble();

            log.info("Processing webhook — ref: {}, status: {}, provider: {}, amount: {} RWF",
                    transactionRef, status, provider, amount);

            updateTransactionFromWebhook(transactionRef, status, provider, fee);

        } catch (Exception e) {
            log.error("Error processing Paypack webhook: {}", e.getMessage(), e);
            // Still return 200 — Paypack will retry if we return the error
        }

        // ALWAYS return 200 to Paypack, even on errors
        // If you return non-200, Paypack will keep retrying
        return ResponseEntity.ok().build();
    }

    /**
     * Paypack sends a HEAD request first to verify the URL is reachable.
     * Must respond 200 to HEAD.
     */
    @RequestMapping(value = "/paypack", method = RequestMethod.HEAD)
    public ResponseEntity<Void> handleHead() {
        return ResponseEntity.ok().build();
    }

    private void updateTransactionFromWebhook(
            String ref, String status, String provider, double fee) {

        transactionRepository.findByTransactionRef(ref).ifPresentOrElse(
                transaction -> {
                    EPaymentStatus newStatus = "successful".equalsIgnoreCase(status)
                            ? EPaymentStatus.SUCCESS
                            : EPaymentStatus.FAILED;

                    transaction.setStatus(newStatus);
                    transaction.setNetwork(provider.toUpperCase());

                    if (newStatus == EPaymentStatus.SUCCESS) {
                        transaction.setCompletedAt(Instant.now());
                        log.info("✅ Payment SUCCESSFUL — ref: {}, amount: {} RWF", ref, fee);
                        // 🔔 ADD YOUR BUSINESS LOGIC HERE:
                        // - Send SMS confirmation
                        // - Fulfill the order
                        // - Publish event to order service
                        // - Notify via WebSocket
                    } else {
                        transaction.setFailureReason("Payment declined or timed out");
                        log.warn("❌ Payment FAILED — ref: {}", ref);
                    }

                    transactionRepository.save(transaction);
                },
                () -> log.warn("⚠️ Webhook for unknown transaction ref: {}", ref)
        );
    }

    /**
     * Verify the webhook came from Paypack using HMAC-SHA256 signature.
     * Algorithm: base64(HMAC-SHA256(rawBody, webhookSecret))
     */
    private boolean verifySignature(String rawBody, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    paypackProperties.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(hmacBytes);

            boolean valid = computed.equals(signature);
            if (!valid) {
                log.warn("Signature mismatch. Expected: {}, Got: {}", computed, signature);
            }
            return valid;
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }
}