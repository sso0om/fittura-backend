package com.fittura.domain.member.controller;

import com.fittura.domain.member.dto.AuthResultDto;
import com.fittura.domain.member.dto.request.SignInReqDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.dto.response.AuthResDto;
import com.fittura.domain.member.service.AuthService;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API (V1)", description = "사용자 인증 및 토큰 관련 API")
public class AuthControllerV1 {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 API")
    public ResponseEntity<RsData<AuthResDto>> signUp(
        @RequestBody @Valid SignUpReqDto signUpReqDto
    ) {
        AuthResultDto resultDto = authService.signUp(signUpReqDto);
        ResponseCookie cookie = authService.generateRefreshTokenCookie(resultDto.tokenDto());
        AuthResDto resDto = AuthResDto.of(resultDto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(RsData.createSuccess("회원가입이 완료되었습니다.", resDto));
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "로그인 API")
    public ResponseEntity<RsData<AuthResDto>> signIn(
        @RequestBody @Valid SignInReqDto signInReqDto
    ) {
        AuthResultDto resultDto = authService.signIn(signInReqDto);
        ResponseCookie cookie = authService.generateRefreshTokenCookie(resultDto.tokenDto());
        AuthResDto resDto = AuthResDto.of(resultDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(RsData.success("로그인되었습니다.", resDto));
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "토큰 재발급 API")
    public ResponseEntity<RsData<AuthResDto>> reissueTokens(
        @CookieValue(name = "${app.cookie.refresh-token-name}") String refreshToken
    ) {
        AuthResultDto resultDto = authService.reissueTokens(refreshToken);
        ResponseCookie cookie = authService.generateRefreshTokenCookie(resultDto.tokenDto());
        AuthResDto resDto = AuthResDto.of(resultDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(RsData.success("토큰이 재발급되었습니다.", resDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 API")
    public ResponseEntity<RsData<Void>> logout() {
        ResponseCookie cookie = authService.createLogoutCookie();

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(RsData.success("로그아웃되었습니다.", null));
    }
}
