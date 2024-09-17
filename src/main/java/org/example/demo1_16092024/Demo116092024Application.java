package org.example.demo1_16092024;

import org.example.demo1_16092024.config.BankConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BankConfig.class)
public class Demo116092024Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo116092024Application.class, args);
    }
}
