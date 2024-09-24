package org.example.demo1_16092024.controller;

import jakarta.validation.Valid;
import org.example.demo1_16092024.config.BankConfig;
import org.example.demo1_16092024.dto.PaymentRequest;
import org.example.demo1_16092024.dto.PaymentResponse;
import org.example.demo1_16092024.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    PaymentService paymentService;

    @Autowired
    BankConfig bankConfig;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Received payment request");

        // Gọi PaymentService để xử lý logic
        PaymentResponse response = paymentService.processPayment(request, bankConfig);

        // Trả về response từ PaymentService
        return ResponseEntity.ok(response);
    }
}
