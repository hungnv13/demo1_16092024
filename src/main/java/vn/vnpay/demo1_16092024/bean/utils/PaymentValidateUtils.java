package vn.vnpay.demo1_16092024.bean.utils;

import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidateUtils {

    private static final Logger logger = LoggerFactory.getLogger(PaymentValidateUtils.class);

    public boolean validateFields(PaymentRequest request) {
        logger.info("Validating fields for PaymentRequest...");

        boolean isValid = true;

        isValid &= validateField(request.getTokenKey(), "tokenKey");
        isValid &= validateField(request.getApiID(), "apiID");
        isValid &= validateField(request.getMobile(), "mobile");
        isValid &= validateField(request.getBankCode(), "bankCode");
        isValid &= validateField(request.getAccountNo(), "accountNo");
        isValid &= validateField(request.getPayDate(), "payDate");
        isValid &= validateField(request.getAdditionalData(), "additionalData");
        isValid &= validateDebitAmount(request.getDebitAmount());
        isValid &= validateField(request.getRespCode(), "respCode");
        isValid &= validateField(request.getRespDesc(), "respDesc");
        isValid &= validateField(request.getTraceTransfer(), "traceTransfer");
        isValid &= validateField(request.getMessageType(), "messageType");
        isValid &= validateField(request.getCheckSum(), "checkSum");
        isValid &= validateField(request.getOrderCode(), "orderCode");
        isValid &= validateField(request.getUserName(), "userName");
        isValid &= validateRealAmount(request.getRealAmount());
        isValid &= validateField(request.getPromotionCode(), "promotionCode");

        if (isValid) {
            logger.info("All fields in PaymentRequest are valid.");
        } else {
            logger.warn("Some fields in PaymentRequest are invalid.");
        }

        return isValid;
    }

    private boolean validateField(String field, String fieldName) {
        if (field == null || field.trim().isEmpty()) {
            logger.warn("Invalid {}: {}", fieldName, field);
            return false;
        }
        return true;
    }

    private boolean validateDebitAmount(Integer debitAmount) {
        if (debitAmount == null) {
            logger.warn("Invalid debitAmount: {}", debitAmount);
            return false;
        }
        return true;
    }

    private boolean validateRealAmount(String realAmount) {
        if (!validateField(realAmount, "realAmount")) {
            return false;
        }
        try {
            Integer.parseInt(realAmount);
        } catch (NumberFormatException e) {
            logger.warn("Invalid realAmount format: {}", realAmount);
            return false;
        }
        return true;
    }
}
