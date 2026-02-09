package com.seatwise.notification_service.service;

import dto.NotificationRequest;
import dto.NotificationResponse;
import enums.ENotificationType;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse createNotification(@Valid NotificationRequest dto, String userTimeZone);

    Page<NotificationResponse> getAllNotifications(UUID userId, Pageable pageable, String userTimeZone);

    Page<NotificationResponse> getReadNotifications(UUID userId, Pageable pageable, String userTimeZone);

    Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable, String userTimeZone);

    NotificationResponse getNotificationById(UUID id, String userTimeZone);

    NotificationResponse markAsRead(UUID id, String userTimeZone);

    Page<NotificationResponse> searchNotifications(UUID userId, String keyword, ENotificationType type, Boolean isRead, Pageable pageable, String userTimeZone);

    void deleteNotification(UUID id);
}
