package com.seatwise.event_service.client;

import dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Slf4j
@Component
public class BookingServiceClient {
    private final WebClient webClient;

    public BookingServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.booking-service.url}")
            String bookingServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(bookingServiceUrl).build();
    }

    public void deleteBookingBySeatId(UUID seatId) {
        try {
            log.info("Deleting booking for seat: {} from booking-service", seatId);
            BaseResponse<Void> response = webClient.delete()
                    .uri("/api/v1/bookings/seat/{seatId}", seatId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<BaseResponse<Void>>() {
                    })
                    .block();
            
            if (response != null && response.isSuccess()) {
                log.info("Booking for seat {} deleted successfully", seatId);
            } else {
                log.warn("Failed to delete booking for seat {}: {}", seatId, 
                        (response != null ? response.getMessage() : "Unknown error"));
            }
        } catch (WebClientResponseException.NotFound e) {
            log.info("No booking found for seat {} in booking-service", seatId);
        } catch (WebClientResponseException e) {
            log.error("Error calling booking-service: {} - {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting booking: {}", e.getMessage());
        }
    }
}
