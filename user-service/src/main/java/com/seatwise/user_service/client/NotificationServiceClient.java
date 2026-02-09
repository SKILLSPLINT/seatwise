package com.seatwise.user_service.client;

import dto.BaseResponse;
import dto.NotificationRequest;
import dto.NotificationResponse;
import exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Slf4j
@Component
public class NotificationServiceClient {
    private final WebClient webClient;

    public NotificationServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.notification-service.url}") String notificationServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(notificationServiceUrl).build();
    }

    public NotificationResponse createNotification(NotificationRequest dto) {
        try {
            BaseResponse<dto.NotificationResponse> response = webClient.post()
                    .uri("/api/v1/notifications")
                    .body(BodyInserters.fromValue(dto))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<dto.NotificationResponse>>() {
                    })
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("Notification created successfully: id={}", response.getData().getId());
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

    public Page<NotificationResponse> getNotifications(UUID userId) {
        try {
            BaseResponse<Page<NotificationResponse>> response = webClient.get()
                    .uri("/api/v1/notifications/user/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<Page<NotificationResponse>>>() {
                    }).block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("Notification created successfully: id={}", response.getData().getContent().get(0).getId());
                return response.getData();
            }

            throw new RuntimeException("Failed to get notifications: " +
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
