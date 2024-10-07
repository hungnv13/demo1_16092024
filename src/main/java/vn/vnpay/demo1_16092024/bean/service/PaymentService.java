package vn.vnpay.demo1_16092024.bean.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.vnpay.demo1_16092024.bean.config.BankConfig;
import vn.vnpay.demo1_16092024.bean.constant.PaymentErrorCode;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import vn.vnpay.demo1_16092024.bean.dto.response.PaymentResponse;
import vn.vnpay.demo1_16092024.bean.utils.PaymentUtils;
import vn.vnpay.demo1_16092024.bean.utils.PaymentValidateUtils;
import vn.vnpay.demo1_16092024.bean.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static vn.vnpay.demo1_16092024.bean.utils.PaymentUtils.encodeHmacSha256;

@Service
public class PaymentService implements IPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private RedisUtils putDataRedis;

    @Autowired
    private PaymentValidateUtils paymentValidateUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentResponse processPayment(PaymentRequest request, BankConfig bankConfig) {
        logger.info("Starting input data validation for request: {}", request);

        if (!validateRequest(request)) {
            return buildErrorResponse(PaymentErrorCode.INVALID_INPUT);
        }

        Optional<BankConfig.Bank> bankOptional = findBankByCode(request.getBankCode(), bankConfig);
        if (bankOptional.isEmpty()) {
            logger.info("Bank code not found: {}", request.getBankCode());
            return buildErrorResponse(PaymentErrorCode.BANK_CODE_NOT_FOUND);
        }

        BankConfig.Bank bank = bankOptional.get();
        logger.info("Bank found for code: {}. Proceeding with checksum validation.", request.getBankCode());

        String calculatedCheckSum = calculateChecksum(request, bank);
        if (null == calculatedCheckSum || calculatedCheckSum.equals(request.getCheckSum())) {
            logger.info("Invalid checksum: {}. Expected: {}", request.getCheckSum(), calculatedCheckSum);
            return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
        }

        String requestAsString = convertRequestToJson(request);
        if (null == requestAsString) {
            return buildErrorResponse(PaymentErrorCode.SYSTEM_ERROR);
        }

        logger.info("Writing data to Redis for bankCode: {}, tokenKey: {}", request.getBankCode(), request.getTokenKey());
        if (!putDataRedis.putData(request.getBankCode(), request.getTokenKey(), requestAsString)) {
            logger.info("Failed to write data to Redis for tokenKey: {}", request.getTokenKey());
            return buildErrorResponse(PaymentErrorCode.SYSTEM_ERROR);
        }

        logger.info("Request successfully processed for bankCode: {}. Returning success response.", request.getBankCode());
        return buildSuccessResponse(PaymentErrorCode.SUCCESS, bank.getPrivateKey());
    }

    private boolean validateRequest(PaymentRequest request) {
        if (!paymentValidateUtils.validateFields(request)) {
            logger.info("Validation failed: Invalid input data for request: {}", request);
            return false;
        }
        return true;
    }

    private String calculateChecksum(PaymentRequest request, BankConfig.Bank bank) {
        try {
            logger.info("Calculating checksum for request: {}", request);
            return calculateRequestCheckSum(request, bank.getPrivateKey());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error calculating checksum for request: {}", request, e);
            return null;
        }
    }

    private String convertRequestToJson(PaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            logger.error("Error converting request to JSON string", e);
            return null;
        }
    }

    @Override
    public String calculateRequestCheckSum(PaymentRequest request, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException {
        logger.info("Start calculating request checksum for PaymentRequest with mobile: {}", request.getMobile());
        String data = new StringBuilder()
                .append(request.getMobile())
                .append(request.getBankCode())
                .append(request.getAccountNo())
                .append(request.getPayDate())
                .append(request.getDebitAmount())
                .append(request.getRespCode())
                .append(request.getTraceTransfer())
                .append(request.getMessageType())
                .toString();
        String checksum = encodeHmacSha256(data, privateKey);
        logger.info("Calculated request checksum: {}", checksum);
        return checksum;
    }

    @Override
    public String calculateResponseCheckSum(String code, String message, String responseId, String responseTime, String privateKey) throws NoSuchAlgorithmException, InvalidKeyException {
        logger.info("Start calculating response checksum for code: {}, message: {}, responseId: {}, responseTime: {}", code, message, responseId, responseTime);
        String data = new StringBuilder()
                .append(code)
                .append(message)
                .append(responseId)
                .append(responseTime)
                .toString();
        String checksum = encodeHmacSha256(data, privateKey);
        logger.info("Calculated response checksum: {}", checksum);
        return checksum;
    }

    private Optional<BankConfig.Bank> findBankByCode(String bankCode, BankConfig bankConfig) {
        return bankConfig.getBankList().stream()
                .filter(bank -> bank.getBankCode().equals(bankCode))
                .findFirst();
    }

    private PaymentResponse buildErrorResponse(PaymentErrorCode errorCode) {
        return new PaymentResponse(
                errorCode.getCode(), errorCode.getMessage(),
                generateRandomId(), getCurrentTimestamp(), null);
    }

    private PaymentResponse buildSuccessResponse(PaymentErrorCode errorCode, String privateKey) {
        String responseId = generateRandomId();
        String responseTime = getCurrentTimestamp();
        String responseCheckSum = calculateResponseChecksum(errorCode, responseId, responseTime, privateKey);
        return new PaymentResponse(
                errorCode.getCode(), errorCode.getMessage(),
                responseId, responseTime, responseCheckSum);
    }

    private String calculateResponseChecksum(PaymentErrorCode errorCode, String responseId, String responseTime, String privateKey) {
        try {
            return calculateResponseCheckSum(
                    errorCode.getCode(), errorCode.getMessage(),
                    responseId, responseTime, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error calculating response checksum for success response", e);
            return null;
        }
    }

    private String generateRandomId() {
        return PaymentUtils.generateRandomId();
    }

    private String getCurrentTimestamp() {
        return PaymentUtils.getCurrentTimestamp();
    }
}
