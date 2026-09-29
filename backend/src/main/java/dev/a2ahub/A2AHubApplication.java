package dev.a2ahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class A2AHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(A2AHubApplication.class, args);
    }
}
