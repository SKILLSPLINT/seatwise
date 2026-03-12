package com.seatwise.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatwise.notification_service.model.Notification;
import com.seatwise.notification_service.repository.NotificationRepository;
import dto.EmailPayload;
import constants.RabbitConstants;
import enums.ENotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepo;
    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitConstants.EMAIL_QUEUE)
    public void consumeEmail(Message message) throws JsonProcessingException {
        log.info("Received email message from RabbitMQ queue: {}", RabbitConstants.EMAIL_QUEUE);
        int retries = (int) message.getMessageProperties().getHeaders().getOrDefault("x-retries", 0);
        EmailPayload payload = objectMapper.readValue(new String(message.getBody()), EmailPayload.class);
        log.info("Processing email for notificationId: {}", payload.getNotificationId());
        Notification notification = notificationRepo.findById(payload.getNotificationId()).orElseThrow();

        try {
            sendEmail(payload);
            notification.setDelivered_At(Instant.now());
            notification.setStatus(ENotificationStatus.SENT);
            notificationRepo.save(notification);
        } catch (Exception e) {
            if (retries < 3) {
                message.getMessageProperties().setHeader("x-retries", retries + 1);
                rabbitTemplate.send(RabbitConstants.EMAIL_EXCHANGE, RabbitConstants.EMAIL_RETRY_ROUTING_KEY, message);
            } else {
                notification.setDelivered_At(Instant.now());
                notification.setStatus(ENotificationStatus.FAILURE);
                notificationRepo.save(notification);
                throw e;
            }
        }
    }


    private void sendEmail(EmailPayload payload) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(payload.getRecipient());
        message.setSubject(payload.getSubject());
        message.setText(payload.getBody());
        message.setFrom(payload.getSender());
        mailSender.send(message);
    }
}
