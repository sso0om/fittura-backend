package com.fittura.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Cookie cookie) {

    public record Cookie(
        String refreshTokenName,
        String domain,
        String path,
        boolean secure,
        boolean httpOnly,
        String sameSite
    ) {}
}
