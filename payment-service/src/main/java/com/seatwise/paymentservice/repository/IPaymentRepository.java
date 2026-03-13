package com.seatwise.paymentservice.repository;

import com.seatwise.paymentservice.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPaymentRepository  extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByTransactionRef(String ref);
    Optional<Payment> findByOrderReference(String orderRef);
    boolean existsByOrderReference(String orderRef);
}
