package vn.vnpay.demo1_16092024.bean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import vn.vnpay.demo1_16092024.bean.config.BankConfig;

@SpringBootApplication
@EnableConfigurationProperties(BankConfig.class)
public class ApplicationRun {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationRun.class, args);
    }
}
