package vn.vnpay.demo1_16092024.bean.utils;

import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PaymentValidateUtils {

    private static final Logger logger = LoggerFactory.getLogger(PaymentValidateUtils.class);

    public boolean validateFields(PaymentRequest request) {
        logger.info("Validating fields for PaymentRequest...");
        boolean isValid = areFieldsValid(
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
        if (isValid) {
            logger.info("All fields in PaymentRequest are valid.");
        } else {
            logger.warn("Some fields in PaymentRequest are invalid.");
        }

        return isValid;
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
}
