package vn.vnpay.demo1_16092024.bean.utils;

import vn.vnpay.demo1_16092024.bean.dto.PaymentRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PaymentValidateUtils {
    public boolean validateFields(PaymentRequest request) {
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
}
