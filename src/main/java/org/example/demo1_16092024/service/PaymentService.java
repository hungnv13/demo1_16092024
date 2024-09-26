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
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired putDataInRedis putDataRedis;

    @Autowired CheckSumService checkSumService;

    @Autowired ValidationService validationService;

    public PaymentResponse processPayment(PaymentRequest request, BankConfig bankConfig) {
        try {

            if (!validationService.validateFields(request)) {
                logger.error("Validation failed: Invalid input data");
                return buildErrorResponse(PaymentErrorCode.INVALID_INPUT);
            }

            Optional<BankConfig.Bank> bankOptional = findBankByCode(request.getBankCode(), bankConfig);
            if (!bankOptional.isPresent()) {
                logger.error("Bank code not found: {}", request.getBankCode());
                return buildErrorResponse(PaymentErrorCode.BANK_CODE_NOT_FOUND);
            }

            BankConfig.Bank bank = bankOptional.get();

            String calculatedCheckSum = checkSumService.calculateRequestCheckSum(request, bank.getPrivateKey());
            if (calculatedCheckSum.equals(request.getCheckSum())) {
                logger.error("Invalid checksum: {}", request.getCheckSum());
                return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
            }

            boolean isSetSuccessful = putDataRedis.setData(request.getBankCode(), request.getTokenKey(), String.valueOf(request));
            if (!isSetSuccessful) {
                logger.error("Failed to set data in Redis for tokenKey: {}", request.getTokenKey());
                return buildErrorResponse(PaymentErrorCode.REDIS_SET_FAILED);
            }

            return buildSuccessResponse(PaymentErrorCode.SUCCESS, bank.getPrivateKey());

        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating checksum", e);
            return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return buildErrorResponse(PaymentErrorCode.REDIS_SET_FAILED);
        }
    }

    private Optional<BankConfig.Bank> findBankByCode(String bankCode, BankConfig bankConfig) {
        return bankConfig.getBankList().stream()
                .filter(b -> b.getBankCode().equals(bankCode))
                .findFirst();
    }

    private PaymentResponse buildErrorResponse(PaymentErrorCode errorCode) {
        return new PaymentResponse(errorCode.getCode(), errorCode.getMessage(), generateRandomId(), getCurrentTimestamp(), null);
    }

    private PaymentResponse buildSuccessResponse(PaymentErrorCode errorCode, String privateKey) throws NoSuchAlgorithmException {
        String responseId = generateRandomId();
        String responseTime = getCurrentTimestamp();
        String responseCheckSum = checkSumService.calculateResponseCheckSum(errorCode.getCode(), errorCode.getMessage(), responseId, responseTime, privateKey);
        return new PaymentResponse(errorCode.getCode(), errorCode.getMessage(), responseId, responseTime, responseCheckSum);
    }

    private String generateRandomId() {
        return PaymentUtils.generateRandomId();
    }

    private String getCurrentTimestamp() {
        return PaymentUtils.getCurrentTimestamp();
    }
}
