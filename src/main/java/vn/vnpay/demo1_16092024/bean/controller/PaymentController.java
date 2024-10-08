package vn.vnpay.demo1_16092024.bean.controller;

import jakarta.validation.Valid;
import vn.vnpay.demo1_16092024.bean.config.BankConfig;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import vn.vnpay.demo1_16092024.bean.dto.response.PaymentResponse;
import vn.vnpay.demo1_16092024.bean.service.IPaymentService;
import vn.vnpay.demo1_16092024.bean.service.PaymentService;
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
    private IPaymentService iPaymentService;

    @Autowired
    private BankConfig bankConfig;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Received payment request");
        PaymentResponse response = iPaymentService.processPayment(request, bankConfig);
        return ResponseEntity.ok(response);
    }
}
