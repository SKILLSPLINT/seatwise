package com.seatwise.paymentservice.models;

import com.seatwise.paymentservice.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "payments")
public class Payment extends  BaseEntity{
    @Column(name = "user_id",nullable = false)
    private UUID userId;
    @Column(name = "booking_id",nullable = false)
    private UUID bookingId;
    @Column(name = "amount",nullable = false)
    private Double amount;
    @Column(name = "description",nullable = false)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status",nullable = false)
    private EPaymentStatus status;

    @Column(nullable = false, unique = true)
    private String transactionRef;

    @Column(nullable = false, unique = true)
    private String orderReference;

    @Column(nullable = false)
    private String phoneNumber;

    private String network;             // MTN or AIRTEL
    private String failureReason;

    private Instant completedAt;


}
