package com.fittura.global.advice;

import com.fittura.global.error.CommonErrorCode;
import com.fittura.global.rsdata.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
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
        if (body == null) return null;

        // 유효하지 않은 status 값일 경우를 방어
        try {
            HttpStatus status = HttpStatus.valueOf(body.status());
            response.setStatusCode(status);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status code {} in RsData. Falling back to 500.", body.status(), e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

            return RsData.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return body;
    }
}
