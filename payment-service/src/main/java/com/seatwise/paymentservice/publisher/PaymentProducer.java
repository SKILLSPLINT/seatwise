package com.seatwise.paymentservice.publisher;

import constants.RabbitConstants;
import dto.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Publishing payment success event for booking: {}", event.getBookingId());
        rabbitTemplate.convertAndSend(
                RabbitConstants.PAYMENT_EXCHANGE,
                RabbitConstants.PAYMENT_SUCCESS_ROUTING_KEY,
                event
        );
    }
}
