package vn.vnpay.demo1_16092024.bean.dto.response;

import lombok.Data;

@Data
public class PaymentResponse {
    private String code;
    private String message;
    private String responseId;
    private String responseTime;
    private String checkSum;

    public PaymentResponse(String code, String message, String responseId, String responseTime, String checkSum) {
        this.code = code;
        this.message = message;
        this.responseId = responseId;
        this.responseTime = responseTime;
        this.checkSum = checkSum;
    }
}
