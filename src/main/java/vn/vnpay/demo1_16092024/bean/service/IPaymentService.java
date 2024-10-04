package vn.vnpay.demo1_16092024.bean.service;

import vn.vnpay.demo1_16092024.bean.config.BankConfig;
import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;
import vn.vnpay.demo1_16092024.bean.dto.response.PaymentResponse;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface IPaymentService {

    PaymentResponse processPayment(PaymentRequest request, BankConfig bankConfig);

    String calculateRequestCheckSum(PaymentRequest request, String privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException;

    String calculateResponseCheckSum(String code, String message, String responseId, String responseTime, String privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException;
}
