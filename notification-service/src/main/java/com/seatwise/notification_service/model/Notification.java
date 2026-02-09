package com.seatwise.notification_service.model;

import enums.ENotificationStatus;
import enums.ENotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ENotificationStatus status;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ENotificationType type;

    @Column(name = "subject", length = 255,nullable = true)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT",nullable = true)
    private String body;

    @Column(name = "recipient", length = 255, nullable = true)
    private String recipient;

    @Column(name = "sender", length = 255,nullable = true)
    private String sender;

    @Column(name = "delivered_at")
    private Instant delivered_At;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;
}
