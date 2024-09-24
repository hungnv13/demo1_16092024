package org.example.demo1_16092024.service;

import org.example.demo1_16092024.config.BankConfig;
import org.example.demo1_16092024.dto.PaymentErrorCode;
import org.example.demo1_16092024.dto.PaymentRequest;
import org.example.demo1_16092024.dto.PaymentResponse;
import org.example.demo1_16092024.utils.PaymentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private RedisService redisService;

    public PaymentResponse processPayment(PaymentRequest request, BankConfig bankConfig) {
        try {
            // Validate input fields
            if (!validateFields(request)) {
                logger.info("Validation failed: Invalid input data");
                return buildErrorResponse(PaymentErrorCode.INVALID_INPUT);
            }

            // Check if bankCode exists in configuration
            Optional<BankConfig.Bank> bankOptional = findBankByCode(request.getBankCode(), bankConfig);
            if (!bankOptional.isPresent()) {
                logger.info("Bank code not found: {}", request.getBankCode());
                return buildErrorResponse(PaymentErrorCode.BANK_CODE_NOT_FOUND);
            }

            BankConfig.Bank bank = bankOptional.get();

            // Validate checksum
            String calculatedCheckSum = calculateCheckSum(request, bank.getPrivateKey());
            if (calculatedCheckSum.equals(request.getCheckSum())) {
                logger.info("Invalid checksum: {}", request.getCheckSum());
                return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
            }

            // Set data in Redis
            boolean isSetSuccessful = redisService.setData(request.getBankCode(), request.getTokenKey(), String.valueOf(request));
            if (!isSetSuccessful) {
                logger.info("Failed to set data in Redis for tokenKey: {}", request.getTokenKey());
                return buildErrorResponse(PaymentErrorCode.REDIS_SET_FAILED);
            }

            // Successful response
            return buildSuccessResponse(PaymentErrorCode.SUCCESS, bank.getPrivateKey());

        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating checksum", e);
            return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return buildErrorResponse(PaymentErrorCode.REDIS_SET_FAILED);
        }
    }

    private boolean validateFields(PaymentRequest request) {
        return !(request.getTokenKey().isEmpty() || request.getApiID().isEmpty() || request.getMobile().isEmpty() ||
                request.getBankCode().isEmpty() || request.getAccountNo().isEmpty() || request.getPayDate().isEmpty() ||
                request.getAdditionalData().isEmpty() || request.getDebitAmount() == null || request.getRespCode().isEmpty() ||
                request.getRespDesc().isEmpty() || request.getTraceTransfer().isEmpty() || request.getMessageType().isEmpty() ||
                request.getCheckSum().isEmpty() || request.getOrderCode().isEmpty() || request.getUserName().isEmpty() ||
                request.getRealAmount().isEmpty() || request.getPromotionCode().isEmpty());
    }

    private Optional<BankConfig.Bank> findBankByCode(String bankCode, BankConfig bankConfig) {
        return bankConfig.getBankList().stream()
                .filter(b -> b.getBankCode().equals(bankCode))
                .findFirst();
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
        return PaymentUtils.sha256(data);
    }

    private PaymentResponse buildErrorResponse(PaymentErrorCode errorCode) {
        return new PaymentResponse(errorCode.getCode(), errorCode.getMessage(), generateRandomId(), getCurrentTimestamp(), null);
    }

    private PaymentResponse buildSuccessResponse(PaymentErrorCode errorCode, String privateKey) throws NoSuchAlgorithmException {
        String responseId = generateRandomId();
        String responseTime = getCurrentTimestamp();
        String responseCheckSum = calculateResponseCheckSum(errorCode.getCode(), errorCode.getMessage(), responseId, responseTime, privateKey);
        return new PaymentResponse(errorCode.getCode(), errorCode.getMessage(), responseId, responseTime, responseCheckSum);
    }

    private String generateRandomId() {
        return PaymentUtils.generateRandomId();
    }

    private String getCurrentTimestamp() {
        return PaymentUtils.getCurrentTimestamp();
    }
}
