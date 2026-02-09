package com.seatwise.notification_service.service;

import dto.NotificationRequest;
import dto.NotificationResponse;
import com.seatwise.notification_service.model.Notification;
import com.seatwise.notification_service.repository.NotificationRepository;
import enums.ENotificationStatus;
import enums.ENotificationType;
import exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utils.TimeUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepo;

    @Override
    public NotificationResponse createNotification(NotificationRequest dto, String userTimeZone) {
        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setType(dto.getType());
        notification.setSubject(dto.getSubject());
        notification.setBody(dto.getBody());
        notification.setRecipient(dto.getRecipient());
        notification.setSender(dto.getSender());
        notification.setRead(false);
        notification.setStatus(ENotificationStatus.PENDING);
        notificationRepo.save(notification);
        return mapToNotificationResponse(notification, userTimeZone);
    }

    @Override
    public Page<NotificationResponse> getAllNotifications(UUID userId, Pageable pageable, String userTimeZone) {
        return notificationRepo.findByUserId(userId, pageable)
                .map(notification -> mapToNotificationResponse(notification, userTimeZone));
    }

    @Override
    public Page<NotificationResponse> getReadNotifications(UUID userId, Pageable pageable, String userTimeZone) {
        return notificationRepo.findByUserIdAndIsRead(userId, true, pageable)
                .map(notification -> mapToNotificationResponse(notification, userTimeZone));
    }

    @Override
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable, String userTimeZone) {
        return notificationRepo.findByUserIdAndIsRead(userId, false, pageable)
                .map(notification -> mapToNotificationResponse(notification, userTimeZone));
    }

    @Override
    public NotificationResponse getNotificationById(UUID id, String userTimeZone) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return mapToNotificationResponse(notification, userTimeZone);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id, String userTimeZone) {
        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        notificationRepo.save(notification);
        return mapToNotificationResponse(notification, userTimeZone);
    }

    @Override
    public Page<NotificationResponse> searchNotifications(UUID userId, String keyword, ENotificationType type, Boolean isRead, Pageable pageable, String userTimeZone) {
        String searchKeyword = keyword != null ? keyword : "";
        return notificationRepo.searchNotifications(userId, searchKeyword, type, isRead, pageable)
                .map(notification -> mapToNotificationResponse(notification, userTimeZone));
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        if (!notificationRepo.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notificationRepo.deleteById(id);
    }

    private NotificationResponse mapToNotificationResponse(Notification notification, String userTimeZone) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .recipient(notification.getRecipient())
                .sender(notification.getSender())
                .isRead(notification.isRead())
                .status(notification.getStatus())
                .createdAt(TimeUtils.toUserOrUtc(notification.getCreatedAt(), userTimeZone))
                .updatedAt(TimeUtils.toUserOrUtc(notification.getUpdatedAt(), userTimeZone))
                .deliveredAt(TimeUtils.toUserOrUtc(notification.getDelivered_At(), userTimeZone))
                .build();
    }
}
