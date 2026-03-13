package com.seatwise.booking_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatwise.booking_service.client.NotificationServiceClient;
import dto.EmailPayload;
import constants.RabbitConstants;
import dto.NotificationRequest;
import enums.ENotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import dto.NotificationResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationServiceClient notificationServiceClient;

    public void sendEmailNotification(UUID userId, EmailPayload payload) {
        try {
            NotificationRequest dto = NotificationRequest.builder()
                    .userId(userId)
                    .type(ENotificationType.EMAIL)
                    .subject(payload.getSubject())
                    .body(payload.getBody())
                    .recipient(payload.getRecipient())
                    .sender(payload.getSender())
                    .build();
            NotificationResponse notificationResponse = notificationServiceClient.createNotification(dto);
            MessageProperties props = new MessageProperties();
            props.setHeader("x-retries", 0);
            payload.setNotificationId(notificationResponse.getId());
            Message message = new Message(objectMapper.writeValueAsString(payload).getBytes(), props);
            rabbitTemplate.send(RabbitConstants.EMAIL_EXCHANGE, RabbitConstants.EMAIL_ROUTING_KEY, message);
            log.info("Email message sent to RabbitMQ: exchange={}, routingKey={}, notificationId={}",
                    RabbitConstants.EMAIL_EXCHANGE, RabbitConstants.EMAIL_ROUTING_KEY, notificationResponse.getId());
        } catch (Exception e) {
            log.error("Failed to send email notification to RabbitMQ", e);
        }
    }
}
