package vn.vnpay.demo1_16092024.bean.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisUtils {
    @Autowired StringRedisTemplate stringRedisTemplate;

    @Autowired ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    public boolean putData(String bankCode, String tokenKey, String data){
        try{
            logger.info("Attempting to put data into Redis for tokenKey: {}, bankCode: {}", tokenKey, bankCode);
            String jsonData = objectMapper.writeValueAsString(data);
            stringRedisTemplate.opsForHash().put(tokenKey, bankCode, jsonData);
            logger.info("Data successfully stored in Redis for tokenKey: {}, bankCode: {}", tokenKey, bankCode);
            return true;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert data to JSON for tokenKey: {}, bankCode: {}. Error: {}", tokenKey, bankCode, e.getMessage());
            return false;
        }
    }
}
