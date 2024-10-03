package vn.vnpay.demo1_16092024.bean.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo1_16092024.bean.constant.PaymentConstant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PaymentUtils {
    private static final Logger logger = LoggerFactory.getLogger(PaymentUtils.class);

    public static String encodeSha256(String data) throws NoSuchAlgorithmException {
        logger.info("Start encoding data using SHA-256 for input: {}", data);
        MessageDigest digest = MessageDigest.getInstance(PaymentConstant.SHA256);
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte hashedByte : hash) {
            String hex = Integer.toHexString(0xff & hashedByte);
            if (1 == hex.length()) {
                hexString.append(PaymentConstant.ZERO_CHAR);
            }
            hexString.append(hex);
        }
        logger.info("SHA-256 encoding complete. Resulting hash: {}", data);
        return hexString.toString();
    }

    public static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(PaymentConstant.TIMESTAMP_FORMATTER));
    }
}
