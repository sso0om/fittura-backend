package com.fittura.domain.payment.pg.toss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@Profile("toss")
@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentProperties(
    @NotBlank String baseUrl,
    @NotBlank String secretKey,
    @NotNull Duration connectTimeout,
    @NotNull Duration readTimeout
) {
}
