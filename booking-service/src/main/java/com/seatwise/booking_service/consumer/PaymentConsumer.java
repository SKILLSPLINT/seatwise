package com.seatwise.booking_service.consumer;

import com.seatwise.booking_service.service.BookingService;
import constants.RabbitConstants;
import dto.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final BookingService bookingService;

    @RabbitListener(queues = RabbitConstants.BOOKING_CONFIRMATION_QUEUE)
    public void consumePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment success event for booking: {}", event.getBookingId());
        try {
            bookingService.confirmBooking(
                    event.getBookingId(),
                    event.getUserId(),
                    event.getUserEmail(),
                    null // userTimeZone default to UTC in service
            );
        } catch (Exception e) {
            log.error("Error confirming booking from payment event: {}", e.getMessage(), e);
            // In a real scenario, we might want to send this to a DLQ or retry
        }
    }
}
