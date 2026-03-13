package com.seatwise.paymentservice.controllers;

import com.seatwise.paymentservice.services.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PaymentController {
    private  final IPaymentService paymentService;
}
