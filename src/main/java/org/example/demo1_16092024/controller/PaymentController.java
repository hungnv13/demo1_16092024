package org.example.demo1_16092024.controller;

import jakarta.validation.Valid;
import org.example.demo1_16092024.config.BankConfig;
import org.example.demo1_16092024.dto.PaymentRequest;
import org.example.demo1_16092024.dto.PaymentResponse;
import org.example.demo1_16092024.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired BankConfig bankConfig;

    @Autowired RedisService redisService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) throws NoSuchAlgorithmException {
        logger.info("Received payment request");

        // Validate all fields are not null, empty, or blank
        if (isNullOrEmpty(request)) {
            logger.error("Validation failed: Invalid input data");
            return ResponseEntity.ok(new PaymentResponse("01", "Invalid Input Data", generateRandomId(), getCurrentTimestamp(), null));
        }

        // Check if bankCode exists in configuration
        Optional<BankConfig.Bank> bankOptional = bankConfig.getBankList().stream()
                .filter(b -> b.getBankCode().equals(request.getBankCode()))
                .findFirst();

        if (!bankOptional.isPresent()) {
            logger.error("Bank code not found: " + request.getBankCode());
            return ResponseEntity.ok(new PaymentResponse("02", "Bank Code not found", generateRandomId(), getCurrentTimestamp(), null));
        }

        BankConfig.Bank bank = bankOptional.get();

        // Validate checksum
        String calculatedCheckSum = calculateCheckSum(request, bank.getPrivateKey());
        if (calculatedCheckSum.equals(request.getCheckSum())) {
            logger.error("Invalid checksum: " + request.getCheckSum());
            return ResponseEntity.ok(new PaymentResponse("03", "Invalid CheckSum", generateRandomId(), getCurrentTimestamp(), null));
        }

        // Set data in Redis
        boolean isSetSuccessful = redisService.setData(request.getBankCode(), request.getTokenKey(), String.valueOf(request));
        if (!isSetSuccessful) {
            logger.error("Failed to set data in Redis for tokenKey: " + request.getTokenKey());
            return ResponseEntity.ok(new PaymentResponse("99", "Redis Set Failed", generateRandomId(), getCurrentTimestamp(), null));
        }

        // Create successful response
        String responseId = generateRandomId();
        String responseTime = getCurrentTimestamp();
        String responseCheckSum = calculateResponseCheckSum("00", "Success", responseId, responseTime, bank.getPrivateKey());

        logger.info("Payment processed successfully for tokenKey: " + request.getTokenKey());
        return ResponseEntity.ok(new PaymentResponse("00", "Success", responseId, responseTime, responseCheckSum));
    }

    private boolean isNullOrEmpty(PaymentRequest request) {
        return request.getTokenKey().isEmpty() || request.getApiID().isEmpty() || request.getMobile().isEmpty() ||
                request.getBankCode().isEmpty() || request.getAccountNo().isEmpty() || request.getPayDate().isEmpty() ||
                request.getAdditionalData().isEmpty() || request.getDebitAmount() == null || request.getRespCode().isEmpty() ||
                request.getRespDesc().isEmpty() || request.getTraceTransfer().isEmpty() || request.getMessageType().isEmpty() ||
                request.getCheckSum().isEmpty() || request.getOrderCode().isEmpty() || request.getUserName().isEmpty() ||
                request.getRealAmount().isEmpty() || request.getPromotionCode().isEmpty();
    }

    private String calculateCheckSum(PaymentRequest request, String privateKey) throws NoSuchAlgorithmException {
        String data = request.getMobile() + request.getBankCode() + request.getAccountNo() + request.getPayDate() +
                request.getDebitAmount() + request.getRespCode() + request.getTraceTransfer() +
                request.getMessageType() + privateKey;
        return sha256(data);
    }

    private String calculateResponseCheckSum(String code, String message, String responseId, String responseTime, String privateKey) throws NoSuchAlgorithmException {
        String data = code + message + responseId + responseTime + privateKey;
        return sha256(data);
    }

    private String sha256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }
}
