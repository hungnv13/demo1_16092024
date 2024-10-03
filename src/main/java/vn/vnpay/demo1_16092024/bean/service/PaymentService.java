package vn.vnpay.demo1_16092024.bean.service;

import vn.vnpay.demo1_16092024.bean.config.BankConfig;
import vn.vnpay.demo1_16092024.bean.constant.PaymentErrorCode;
import vn.vnpay.demo1_16092024.bean.utils.PaymentValidateUtils;
import vn.vnpay.demo1_16092024.bean.utils.RedisUtils;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import vn.vnpay.demo1_16092024.bean.dto.response.PaymentResponse;
import vn.vnpay.demo1_16092024.bean.utils.PaymentUtils;
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
    RedisUtils putDataRedis;

    @Autowired
    PaymentValidateUtils paymentValidateUtils;

    public PaymentResponse processPayment(PaymentRequest request, BankConfig bankConfig) {
        try {
            logger.info("Starting input data validation for request: {}", request);
            if (!paymentValidateUtils.validateFields(request)) {
                logger.info("Validation failed: Invalid input data for request: {}", request);
                return buildErrorResponse(PaymentErrorCode.INVALID_INPUT);
            }
            logger.info("Validating bank code: {}", request.getBankCode());
            Optional<BankConfig.Bank> bankOptional = findBankByCode(request.getBankCode(), bankConfig);
            if (!bankOptional.isPresent()) {
                logger.info("Bank code not found: {}", request.getBankCode());
                return buildErrorResponse(PaymentErrorCode.BANK_CODE_NOT_FOUND);
            }
            BankConfig.Bank bank = bankOptional.get();
            logger.info("Bank found for code: {}. Proceeding with checksum validation.", request.getBankCode());
            logger.info("Calculating checksum for request: {}", request);
            String calculatedCheckSum = calculateRequestCheckSum(request, bank.getPrivateKey());
            if (calculatedCheckSum.equals(request.getCheckSum())) {
                logger.info("Invalid checksum: {}. Expected: {}", request.getCheckSum(), calculatedCheckSum);
                return buildErrorResponse(PaymentErrorCode.INVALID_CHECKSUM);
            }
            logger.info("Writing data to Redis for bankCode: {}, tokenKey: {}", request.getBankCode(), request.getTokenKey());
            boolean isSetSuccessful = putDataRedis.putData(request.getBankCode(), request.getTokenKey(), String.valueOf(request));
            if (!isSetSuccessful) {
                logger.info("Failed to write data to Redis for tokenKey: {}", request.getTokenKey());
                return buildErrorResponse(PaymentErrorCode.SYSTEM_ERROR);
            }
            logger.info("Request successfully processed for bankCode: {}. Returning success response.", request.getBankCode());
            return buildSuccessResponse(PaymentErrorCode.SUCCESS, bank.getPrivateKey());
        } catch (Exception e) {
            logger.error("Error occurred while processing request: {}", request, e);
            return buildErrorResponse(PaymentErrorCode.SYSTEM_ERROR);
        }

    }

    public String calculateRequestCheckSum(PaymentRequest request, String privateKey)
            throws NoSuchAlgorithmException {
        logger.info("Start calculating checksum for PaymentRequest. Mobile: {}, BankCode: {}, AccountNo: {}, PayDate: {}, DebitAmount: {}, RespCode: {}, TraceTransfer: {}, MessageType: {}",
                request.getMobile(), request.getBankCode(), request.getAccountNo(), request.getPayDate(),
                request.getDebitAmount(), request.getRespCode(), request.getTraceTransfer(), request.getMessageType());
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
        logger.info("Checksum calculation completed. Result: {}", data);
        return sha256(data);
    }

    public String calculateResponseCheckSum(String code, String message, String responseId, String responseTime, String privateKey)
            throws NoSuchAlgorithmException {
        logger.info("Start calculating response checksum. Code: {}, Message: {}, ResponseId: {}, ResponseTime: {}",
                code, message, responseId, responseTime);
        String data = new StringBuilder()
                .append(code)
                .append(message)
                .append(responseId)
                .append(responseTime)
                .append(privateKey)
                .toString();
        logger.info("Response checksum calculation completed. Result: {}", data);
        return sha256(data);
    }

    private String sha256(String data) throws NoSuchAlgorithmException {
        return PaymentUtils.encodeSha256(data);
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

    private PaymentResponse buildSuccessResponse(PaymentErrorCode errorCode, String privateKey)
            throws NoSuchAlgorithmException {
        String responseId = generateRandomId();
        String responseTime = getCurrentTimestamp();
        String responseCheckSum = calculateResponseCheckSum(
                errorCode.getCode(), errorCode.getMessage(),
                responseId, responseTime, privateKey);
        return new PaymentResponse(
                errorCode.getCode(), errorCode.getMessage(),
                responseId, responseTime, responseCheckSum);
    }

    private String generateRandomId() {
        return PaymentUtils.generateRandomId();
    }

    private String getCurrentTimestamp() {
        return PaymentUtils.getCurrentTimestamp();
    }
}
