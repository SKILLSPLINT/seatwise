package com.seatwise.booking_service.client;

import com.seatwise.booking_service.dto.response.SeatResponse;
import dto.BaseResponse;
import exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Slf4j
@Component
public class EventServiceClient {
    private final WebClient webClient;


    public EventServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.event-service.url}") String eventUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(eventUrl).build();
    }


    public SeatResponse bookSeat(UUID seatID, UUID eventID, UUID userID, String userEmail) {
        try {
            BaseResponse<SeatResponse> response = webClient.patch()
                    .uri("/api/v1/events/{eventID}/reserve/{seatId}", eventID, seatID)
                    .header("X-User-Id", String.valueOf(userID))
                    .header("X-User-Email", userEmail)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<SeatResponse>>() {
                    }).block();
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

    public SeatResponse unreserveSeat(UUID seatID, UUID userID, String userEmail) {
        try {
            BaseResponse<SeatResponse> response = webClient.patch()
                    .uri("/api/v1/events/unreserve/{seatId}", seatID)
                    .header("X-User-Id", String.valueOf(userID))
                    .header("X-User-Email", userEmail)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<SeatResponse>>() {
                    }).block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            throw new RuntimeException("Failed to unreserve seat: " +
                    (response != null ? response.getMessage() : "Unknown error"));
        } catch (Exception e) {
            log.error("Error unreserving seat: {}", e.getMessage());
            throw new RuntimeException("Failed to unreserve seat", e);
        }
    }

    public SeatResponse confirmSeat(UUID seatID, UUID userID, String userEmail) {
        try {
            BaseResponse<SeatResponse> response = webClient.patch()
                    .uri("/api/v1/events/confirm/{seatId}", seatID)
                    .header("X-User-Id", String.valueOf(userID))
                    .header("X-User-Email", userEmail)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<SeatResponse>>() {
                    }).block();
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
