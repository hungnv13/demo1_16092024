package org.example.demo1_16092024.service;

import org.example.demo1_16092024.config.BankConfig;
import org.example.demo1_16092024.dto.PaymentErrorCode;
import org.example.demo1_16092024.utils.putDataInRedis;
import org.example.demo1_16092024.dto.PaymentRequest;
import org.example.demo1_16092024.dto.PaymentResponse;
import org.example.demo1_16092024.utils.PaymentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired putDataInRedis putDataRedis;

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
            String calculatedCheckSum = calculateRequestCheckSum(request, bank.getPrivateKey());
            if (calculatedCheckSum.equals(request.getCheckSum())) {
                logger.info("Invalid checksum: {}", request.getCheckSum());
                return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
            }

            // Set data in Redis
            boolean isSetSuccessful = putDataRedis.setData(request.getBankCode(), request.getTokenKey(), String.valueOf(request));
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
        return areFieldsValid(
                request.getTokenKey(),
                request.getApiID(),
                request.getMobile(),
                request.getBankCode(),
                request.getAccountNo(),
                request.getPayDate(),
                request.getAdditionalData(),
                request.getDebitAmount(),
                request.getRespCode(),
                request.getRespDesc(),
                request.getTraceTransfer(),
                request.getMessageType(),
                request.getCheckSum(),
                request.getOrderCode(),
                request.getUserName(),
                request.getRealAmount(),
                request.getPromotionCode()
        );
    }

    private boolean areFieldsValid(Object... fields) {
        return Arrays.stream(fields)
                .allMatch(this::isValidField);
    }

    private boolean isValidField(Object field) {
        if (field instanceof String) {
            return field != null && !((String) field).trim().isEmpty();
        } else if (field instanceof Integer) {
            return field != null;
        }
        return field != null;
    }


    private Optional<BankConfig.Bank> findBankByCode(String bankCode, BankConfig bankConfig) {
        return bankConfig.getBankList().stream()
                .filter(b -> b.getBankCode().equals(bankCode))
                .findFirst();
    }

    private String calculateRequestCheckSum(PaymentRequest request, String privateKey) throws NoSuchAlgorithmException {
        String data = new StringBuilder()
                .append(request.getMobile())
                .append(request.getBankCode())
                .append(request.getAccountNo())
                .append(request.getPayDate())
                .append(request.getDebitAmount())
                .append(request.getRespCode())
                .append(request.getTraceTransfer())
                .append(request.getMessageType())
                .append(privateKey)
                .toString();

        return sha256(data);
    }

    private String calculateResponseCheckSum(String code, String message, String responseId, String responseTime, String privateKey) throws NoSuchAlgorithmException {
        String data = new StringBuilder()
                .append(code)
                .append(message)
                .append(responseId)
                .append(responseTime)
                .append(privateKey)
                .toString();

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
