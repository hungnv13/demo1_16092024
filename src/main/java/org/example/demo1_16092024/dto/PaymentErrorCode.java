package org.example.demo1_16092024.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentErrorCode {
    SUCCESS("00", "Success"),
    INVALID_INPUT("01", "Invalid Input Data"),
    BANK_CODE_NOT_FOUND("02", "Bank Code not found"),
    INVALID_CHECKSUM("03", "Invalid CheckSum"),
    REDIS_SET_FAILED("99", "Redis Set Failed");

    private final String code;
    private final String message;

}
