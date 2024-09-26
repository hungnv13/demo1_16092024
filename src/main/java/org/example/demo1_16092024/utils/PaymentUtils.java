package org.example.demo1_16092024.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PaymentUtils {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String SHA256 = "SHA-256";
    private static final char ZERO_CHAR = '0';

    public static String sha256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(SHA256);
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (1 == hex.length()) {
                hexString.append(ZERO_CHAR);
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    public static String getCurrentTimestamp() {
        return TIMESTAMP_FORMATTER.format(LocalDateTime.now());
    }
}
