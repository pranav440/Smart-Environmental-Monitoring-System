package com.environmental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
        System.out.println("🌍 Smart Environmental Intelligence System Started!");
        System.out.println("📊 Access the dashboard at: http://localhost:8080");
    }
}