package com.fittura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FitturaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitturaApplication.class, args);
    }
}
