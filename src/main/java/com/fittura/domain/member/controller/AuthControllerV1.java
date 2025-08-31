package com.fittura.domain.member.controller;

import com.fittura.domain.member.dto.AuthResultDto;
import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.dto.request.SignInReqDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.dto.response.AuthResDto;
import com.fittura.domain.member.service.AuthService;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API (V1)", description = "사용자 인증 및 토큰 관련 API")
public class AuthControllerV1 {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 API")
    public RsData<AuthResDto> signUp(
        @RequestBody @Valid SignUpReqDto signUpReqDto,
        HttpServletResponse httpServletResponse
    ) {
        AuthResultDto resultDto = authService.signUp(signUpReqDto);
        AuthResDto resDto = processAuthResult(resultDto, httpServletResponse);

        return RsData.createSuccess(
            "회원가입이 완료되었습니다.",
            resDto
        );
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "로그인 API")
    public RsData<AuthResDto> signIn(
        @RequestBody @Valid SignInReqDto signInReqDto,
        HttpServletResponse httpServletResponse
    ) {
        AuthResultDto resultDto = authService.signIn(signInReqDto);
        AuthResDto resDto = processAuthResult(resultDto, httpServletResponse);

        return RsData.success(
            "로그인되었습니다.",
            resDto
        );
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "토큰 재발급 API")
    public RsData<AuthResDto> reissueTokens(
        @CookieValue(name = "${app.cookie.refresh-token-name}") String refreshToken,
        HttpServletResponse httpServletResponse
    ) {
        AuthResultDto resultDto = authService.reissueTokens(refreshToken);
        AuthResDto resDto = processAuthResult(resultDto, httpServletResponse);

        return RsData.success(
            "토큰이 재발급되었습니다.",
            resDto
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 API")
    public RsData<Void> logout(
        HttpServletResponse httpServletResponse
    ) {
        ResponseCookie cookie = authService.createLogoutCookie();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return RsData.success(
            "로그아웃되었습니다.",
            null
        );
    }


    // ========== 헬퍼 메서드 ==========

    /**
     * 회원가입 또는 로그인 성공 후 공통으로 수행되는 후처리 작업을 담당
     * 1. Refresh Token 쿠키를 HttpServletResponse에 추가
     * 2. Access Token을 포함한 AuthResDto를 생성하여 반환
     *
     * @param authResultDto       인증 서비스의 결과(사용자 정보, 토큰)를 담은 DTO
     * @param httpServletResponse 쿠키를 헤더에 추가하기 위한 서블릿 응답 객체
     * @return                    API 응답 본문에 포함될 DTO
     */
    private AuthResDto processAuthResult(AuthResultDto authResultDto, HttpServletResponse httpServletResponse) {
        TokenDto tokenDto = authResultDto.tokenDto();

        ResponseCookie cookie = authService.generateRefreshTokenCookie(tokenDto);
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return AuthResDto.of(authResultDto);
    }
}
