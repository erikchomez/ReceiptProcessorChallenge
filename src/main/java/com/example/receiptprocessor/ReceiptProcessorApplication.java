package com.example.receiptprocessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
@Slf4j
public class ReceiptProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptProcessorApplication.class, args);
    }

}
