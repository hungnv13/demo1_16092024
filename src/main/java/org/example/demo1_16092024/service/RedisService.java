package org.example.demo1_16092024.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Autowired StringRedisTemplate stringRedisTemplate;
    @Autowired ObjectMapper objectMapper;

    public boolean setData(String bankCode, String tokenKey, String data){
        try{
            String jsonData = objectMapper.writeValueAsString(data);
            stringRedisTemplate.opsForHash().put(tokenKey, bankCode, jsonData);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
