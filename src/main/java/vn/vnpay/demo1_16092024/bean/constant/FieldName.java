package vn.vnpay.demo1_16092024.bean.constant;

public enum FieldName {
    TOKEN_KEY("tokenKey"),
    API_ID("apiID"),
    MOBILE("mobile"),
    BANK_CODE("bankCode"),
    ACCOUNT_NO("accountNo"),
    PAY_DATE("payDate"),
    ADDITIONAL_DATA("additionalData"),
    DEBIT_AMOUNT("debitAmount"),
    RESP_CODE("respCode"),
    RESP_DESC("respDesc"),
    TRACE_TRANSFER("traceTransfer"),
    MESSAGE_TYPE("messageType"),
    CHECKSUM("checkSum"),
    ORDER_CODE("orderCode"),
    USER_NAME("userName"),
    REAL_AMOUNT("realAmount"),
    PROMOTION_CODE("promotionCode");

    private final String fieldName;

    FieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
}
