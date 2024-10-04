package vn.vnpay.demo1_16092024.bean.repository;

import vn.vnpay.demo1_16092024.bean.dto.request.PaymentRequest;

public interface PaymentValidatorRepository {
    boolean validateFields(PaymentRequest request);
}

