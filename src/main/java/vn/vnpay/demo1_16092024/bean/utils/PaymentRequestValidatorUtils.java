package vn.vnpay.demo1_16092024.bean.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import vn.vnpay.demo1_16092024.bean.repository.PaymentValidatorRepository;

public class PaymentRequestValidatorUtils implements PaymentValidatorRepository {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRequestValidatorUtils.class);

    @Override
    public boolean validateFields(PaymentRequest request) {
        return validateBankCode(request.getBankCode()) &&
                validateTokenKey(request.getTokenKey()) &&
                validateOtherFields(request);
    }

    private boolean validateBankCode(String bankCode) {
        if (bankCode == null || bankCode.isEmpty()) {
            logger.info("Invalid bank code: {}", bankCode);
            return false;
        }
        logger.info("Bank code validation passed: {}", bankCode);
        return true;
    }

    private boolean validateTokenKey(String tokenKey) {
        if (tokenKey == null || tokenKey.isEmpty()) {
            logger.info("Invalid token key: {}", tokenKey);
            return false;
        }
        logger.info("Token key validation passed: {}", tokenKey);
        return true;
    }

    private boolean validateOtherFields(PaymentRequest request) {
        return true;
    }
}

