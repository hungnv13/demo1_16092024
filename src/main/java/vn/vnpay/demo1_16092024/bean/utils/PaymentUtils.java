package vn.vnpay.demo1_16092024.bean.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.demo1_16092024.bean.constant.PaymentConstant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


public class PaymentUtils {
    private static final Logger logger = LoggerFactory.getLogger(PaymentUtils.class);

    public static String encodeHmacSha256(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        logger.info("Start encoding data using HMAC SHA-256.");
        Mac hmacSHA256 = Mac.getInstance(PaymentConstant.ENCODESHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte hashedByte : hash) {
            String hex = Integer.toHexString(0xff & hashedByte);
            if (1 == hex.length()) {
                hexString.append(PaymentConstant.ZERO_CHAR);
            }
            hexString.append(hex);
        }

        String checksum = hexString.toString();
        logger.info("HMAC SHA-256 encoding complete. Resulting hash: {}", checksum);
        return checksum;
    }


    public static String generateRandomId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(PaymentConstant.TIMESTAMP_FORMATTER));
    }
}
