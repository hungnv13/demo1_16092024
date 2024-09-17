package org.example.demo1_16092024.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
