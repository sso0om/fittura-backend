package com.fittura.domain.member.controller;

import com.fittura.domain.member.dto.SignUpResultDto;
import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.dto.response.SignUpResDto;
import com.fittura.domain.member.service.AuthService;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API (V1)", description = "사용자 인증 및 토큰 관련 API")
public class AuthControllerV1 {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 API")
    public RsData<SignUpResDto> signUp(
        @RequestBody @Valid SignUpReqDto signUpReqDto,
        HttpServletResponse httpServletResponse
    ) {
        SignUpResultDto resultDto = authService.signUp(signUpReqDto);
        TokenDto tokenDto = resultDto.tokenDto();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenDto.refreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofMillis(tokenDto.refreshTokenExpiresInMillis()))
            .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        SignUpResDto resDto = SignUpResDto.from(resultDto, tokenDto.accessToken());

        return RsData.createSuccess(
            "회원가입이 완료되었습니다.",
            resDto
        );
    }
}
