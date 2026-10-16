package com.fittura.domain.payment.pg.toss;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("toss")
public class TossPaymentConfig {

    @Bean
    public RestClient tossRestClient(TossPaymentProperties props) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(props.connectTimeout());
        requestFactory.setReadTimeout(props.readTimeout());

        return RestClient.builder()
            .baseUrl(props.baseUrl())
            .defaultHeaders(headers -> headers.setBasicAuth(props.secretKey(), ""))
            .requestFactory(requestFactory)
            .build();
    }
}
