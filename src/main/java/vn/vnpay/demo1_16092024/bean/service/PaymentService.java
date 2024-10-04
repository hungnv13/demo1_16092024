package vn.vnpay.demo1_16092024.bean.service;

import vn.vnpay.demo1_16092024.bean.config.BankConfig;
import vn.vnpay.demo1_16092024.bean.constant.PaymentErrorCode;
import vn.vnpay.demo1_16092024.bean.repository.PaymentValidatorRepository;
import vn.vnpay.demo1_16092024.bean.utils.PaymentRequestValidatorUtils;
import vn.vnpay.demo1_16092024.bean.utils.PaymentValidateUtils;
import vn.vnpay.demo1_16092024.bean.utils.RedisUtils;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import vn.vnpay.demo1_16092024.bean.dto.response.PaymentResponse;
import vn.vnpay.demo1_16092024.bean.utils.PaymentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static vn.vnpay.demo1_16092024.bean.utils.PaymentUtils.encodeHmacSha256;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    RedisUtils putDataRedis;

    @Autowired
    PaymentValidateUtils paymentValidateUtils;

    public PaymentResponse processPayment(PaymentRequest request, BankConfig bankConfig) {
        PaymentValidatorRepository validator = new PaymentRequestValidatorUtils();

        try {
            logger.info("Starting input data validation for request: {}", request);
            if (!validator.validateFields(request)) {
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

            String requestAsString = String.valueOf(request);
            logger.info("Converted request to string: {}", requestAsString);
            logger.info("Writing data to Redis for bankCode: {}, tokenKey: {}", request.getBankCode(), request.getTokenKey());

            boolean isSetSuccessful = putDataRedis.putData(request.getBankCode(), request.getTokenKey(), requestAsString);
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

    private PaymentResponse buildSuccessResponse(PaymentErrorCode errorCode, String privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
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
