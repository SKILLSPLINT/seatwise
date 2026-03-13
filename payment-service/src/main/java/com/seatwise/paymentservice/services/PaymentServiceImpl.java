package com.seatwise.paymentservice.services;

import com.seatwise.paymentservice.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class PaymentServiceImpl implements  IPaymentService{
    private  final IPaymentRepository paymentRepo;
}
