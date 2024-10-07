package vn.vnpay.demo1_16092024.bean.utils;

import vn.vnpay.demo1_16092024.bean.constant.FieldName;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidateUtils {

    private static final Logger logger = LoggerFactory.getLogger(PaymentValidateUtils.class);

    public boolean validateFields(PaymentRequest request) {
        logger.info("Validating fields for PaymentRequest...");

        boolean allFieldsValid = true;

        allFieldsValid &= validateStringField(request.getTokenKey(), FieldName.TOKEN_KEY);
        allFieldsValid &= validateStringField(request.getApiID(), FieldName.API_ID);
        allFieldsValid &= validateStringField(request.getMobile(), FieldName.MOBILE);
        allFieldsValid &= validateStringField(request.getBankCode(), FieldName.BANK_CODE);
        allFieldsValid &= validateStringField(request.getAccountNo(), FieldName.ACCOUNT_NO);
        allFieldsValid &= validateStringField(request.getPayDate(), FieldName.PAY_DATE);
        allFieldsValid &= validateStringField(request.getAdditionalData(), FieldName.ADDITIONAL_DATA);
        allFieldsValid &= validateIntegerField(request.getDebitAmount(), FieldName.DEBIT_AMOUNT);
        allFieldsValid &= validateStringField(request.getRespCode(), FieldName.RESP_CODE);
        allFieldsValid &= validateStringField(request.getRespDesc(), FieldName.RESP_DESC);
        allFieldsValid &= validateStringField(request.getTraceTransfer(), FieldName.TRACE_TRANSFER);
        allFieldsValid &= validateStringField(request.getMessageType(), FieldName.MESSAGE_TYPE);
        allFieldsValid &= validateStringField(request.getCheckSum(), FieldName.CHECKSUM);
        allFieldsValid &= validateStringField(request.getOrderCode(), FieldName.ORDER_CODE);
        allFieldsValid &= validateStringField(request.getUserName(), FieldName.USER_NAME);
        allFieldsValid &= validateRealAmount(request.getRealAmount());
        allFieldsValid &= validateStringField(request.getPromotionCode(), FieldName.PROMOTION_CODE);

        if (allFieldsValid) {
            logger.info("All fields in PaymentRequest are valid.");
        } else {
            logger.warn("Some fields in PaymentRequest are invalid.");
        }

        return allFieldsValid;
    }

    private boolean validateStringField(String field, FieldName fieldName) {
        if (isNullOrEmpty(field)) {
            logger.warn("Invalid {}: {}", fieldName, field);
            return false;
        }
        return true;
    }

    private boolean validateIntegerField(Integer field, FieldName fieldName) {
        if (field == null) {
            logger.warn("Invalid {}: {}", fieldName, field);
            return false;
        }
        return true;
    }

    private boolean validateRealAmount(String realAmount) {
        if (!validateStringField(realAmount, FieldName.REAL_AMOUNT)) {
            return false;
        }
        if (!isNumeric(realAmount)) {
            logger.warn("Invalid realAmount format: {}", realAmount);
            return false;
        }
        return true;
    }

    private boolean isNullOrEmpty(String field) {
        return field == null || field.trim().isEmpty();
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
