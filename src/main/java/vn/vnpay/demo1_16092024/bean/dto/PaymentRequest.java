package vn.vnpay.demo1_16092024.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotBlank
    private String tokenKey;

    @NotBlank
    private String apiID;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
    private String mobile;

    @NotBlank
    private String bankCode;

    @NotBlank
    private String accountNo;

    @NotBlank
    @Pattern(regexp = "\\d{14}", message = "PayDate must be in the format yyyyMMddHHmmss")
    private String payDate;

    @NotBlank
    private String additionalData;

    @NotNull
    private Integer debitAmount;

    @NotBlank
    private String respCode;

    @NotBlank
    private String respDesc;

    @NotBlank
    private String traceTransfer;

    @NotBlank
    private String messageType;

    @NotBlank
    private String checkSum;

    @NotBlank
    private String orderCode;

    @NotBlank
    private String userName;

    @NotBlank
    private String realAmount;

    @NotBlank
    private String promotionCode;

}
