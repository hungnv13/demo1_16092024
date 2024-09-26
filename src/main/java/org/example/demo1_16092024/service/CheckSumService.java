package org.example.demo1_16092024.service;

import org.example.demo1_16092024.dto.PaymentRequest;
import org.example.demo1_16092024.utils.PaymentUtils;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public class CheckSumService {
    public String calculateRequestCheckSum(PaymentRequest request, String privateKey) throws NoSuchAlgorithmException {
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

        return sha256(data);
    }

    public String calculateResponseCheckSum(String code, String message, String responseId, String responseTime, String privateKey) throws NoSuchAlgorithmException {
        String data = new StringBuilder()
                .append(code)
                .append(message)
                .append(responseId)
                .append(responseTime)
                .append(privateKey)
                .toString();

        return sha256(data);
    }

    private String sha256(String data) throws NoSuchAlgorithmException {
        return PaymentUtils.sha256(data);
    }
}
