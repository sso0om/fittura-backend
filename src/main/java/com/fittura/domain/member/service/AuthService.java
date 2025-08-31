package com.fittura.domain.member.service;

import com.fittura.domain.member.dto.AuthResultDto;
import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.dto.request.SignInReqDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.MemberErrorCode;
import com.fittura.global.config.AppProperties;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final TokenService tokenService;

    @Transactional
    public AuthResultDto signUp(SignUpReqDto req) {
        // 1. 입력값 정규화
        final String email = req.email().trim();
        final String name = req.name().trim();
        final String nickname = req.nickname().trim();

        // 2. 중복 검사
        memberService.validateSignUpRequest(email, nickname);

        Member member = Member.createUser(
            email,
            name,
            nickname,
            passwordEncoder.encode(req.password())
        );
        Member savedMember = memberService.createMember(member);

        TokenDto tokenDto = tokenService.issueTokens(savedMember);

        return AuthResultDto.of(savedMember, tokenDto);
    }

    @Transactional(readOnly = true)
    public AuthResultDto signIn(SignInReqDto req) {
        final String email = req.email().trim();

        Member member = memberService.findByEmail(email);
        validatePassword(req, member);

        TokenDto tokenDto = tokenService.issueTokens(member);

        return AuthResultDto.of(member, tokenDto);
    }

    @Transactional(readOnly = true)
    public AuthResultDto reissueTokens(String refreshToken) {
        Long memberId = tokenService.findMemberIdByRefreshToken(refreshToken);
        Member member = memberService.findById(memberId);

        TokenDto tokenDto = tokenService.issueTokens(member);
        return AuthResultDto.of(member, tokenDto);
    }


    // ========== 리프래시 토큰 쿠키 관련 메서드 ==========

    public ResponseCookie generateRefreshTokenCookie(TokenDto tokenDto) {
        long refreshTokenExpiresInMillis = tokenService.getRefreshTokenExpiresInMillis();

        return baseRefreshCookieBuilder(tokenDto.refreshToken())
            .maxAge(Duration.ofMillis(refreshTokenExpiresInMillis))
            .build();
    }

    public ResponseCookie createLogoutCookie() {
        return baseRefreshCookieBuilder("")
            .maxAge(Duration.ZERO)
            .build();
    }


    // ========== 헬퍼 메서드 ==========

    private void validatePassword(SignInReqDto req, Member member) {
        if (!passwordEncoder.matches(req.password(), member.getPassword())) {
            throw new ServiceException(MemberErrorCode.INVALID_CREDENTIALS);
        }
    }

    private ResponseCookie.ResponseCookieBuilder baseRefreshCookieBuilder(String value) {
        AppProperties.Cookie cookieProps = appProperties.cookie();

        return ResponseCookie.from(cookieProps.refreshTokenName(), value)
            .httpOnly(cookieProps.httpOnly())
            .secure(cookieProps.secure())
            .domain(cookieProps.domain())
            .path(cookieProps.path())
            .sameSite(cookieProps.sameSite());
    }
}
