package com.fittura.global.advice;

import com.fittura.global.rsdata.RsData;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class RsDataResponseBodyAdvice implements ResponseBodyAdvice<RsData<?>> {
    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        // 컨트롤러의 반환 타입이 RsData 클래스인 경우에만 이 Advice를 적용
        return RsData.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public RsData<?> beforeBodyWrite(
            RsData<?> body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        response.setStatusCode(HttpStatus.valueOf(body.status()));
        return body;
    }
}
