package com.seatwise.booking_service.client;

import com.seatwise.booking_service.dto.request.PaymentRequest;
import com.seatwise.booking_service.dto.response.PaymentResponse;
import dto.BaseResponse;
import exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Component
@Slf4j
public class PaymentClient {
    private final WebClient webClient;

    public PaymentClient(WebClient.Builder webClient,
                         @Value("${services.payment-service.url}") String baseUrl
    ) {
        this.webClient = webClient.baseUrl(baseUrl).build();
    }

    public PaymentResponse initiatePayment(String phoneNumber, UUID bookingId, Double amount, UUID userId, String orderReference, String description) {
        try {
            BaseResponse<PaymentResponse> response = webClient.post()
                    .uri("/api/v1/payments/momo")
                    .bodyValue(new PaymentRequest(
                            phoneNumber,
                            amount,
                            bookingId,
                            userId,
                            orderReference,
                            description
                    ))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<PaymentResponse>>() {
                    })
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            throw new RuntimeException("Failed to create notification: " +
                    (response != null ? response.getMessage() : "Unknown error"));
        } catch (WebClientResponseException.NotFound e) {
            log.error("Notification service endpoint not found: {}", e.getMessage());
            throw new ResourceNotFoundException("Notification service endpoint", "uri", "/api/v1/notifications");
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            throw new RuntimeException("Failed to send notification", e);
        }

    }
}
