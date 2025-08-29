package com.fittura.domain.member.service;

import com.fittura.domain.member.dto.SignUpResultDto;
import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.AuthError;
import com.fittura.domain.member.repository.MemberRepository;
import com.fittura.global.config.AppProperties;
import com.fittura.global.error.CommonErrorCode;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final TokenService tokenService;

    @Transactional
    public SignUpResultDto signUp(SignUpReqDto req) {
        // 1. 입력값 정규화
        final String email = req.email().trim();
        final String name = req.name().trim();
        final String nickname = req.nickname().trim();

        // 2. 중복 검사
        validateSignUpRequest(email, nickname);

        Member member = Member.createUser(
            email,
            name,
            nickname,
            passwordEncoder.encode(req.password())
        );

        Member savedMember;
        try {
            // 3. DB 저장
            savedMember = memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // 4. 동시성 문제로 인한 DB 유니크 제약 위반 처리
            if (memberRepository.existsByEmail(email)) {
                throw new ServiceException(AuthError.DUPLICATED_EMAIL);
            }
            if (memberRepository.existsByNickname(nickname)) {
                throw new ServiceException(AuthError.DUPLICATED_NICKNAME);
            }
            log.error("Unknown DataIntegrityViolationException during sign up: email={}, nickname={}", email, nickname, e);
            throw new ServiceException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        TokenDto tokenDto = tokenService.issueTokens(savedMember);

        return new SignUpResultDto(
            savedMember.getId(),
            savedMember.getEmail(),
            savedMember.getNickname(),
            tokenDto
        );
    }

    public ResponseCookie generateRefreshTokenCookie(TokenDto tokenDto) {
        AppProperties.Cookie cookieProps = appProperties.cookie();

        return ResponseCookie.from(cookieProps.refreshTokenName(), tokenDto.refreshToken())
            .httpOnly(cookieProps.httpOnly())
            .secure(cookieProps.secure())
            .domain(cookieProps.domain())
            .path(cookieProps.path())
            .maxAge(Duration.ofMillis(tokenDto.refreshTokenExpiresInMillis()))
            .sameSite(cookieProps.sameSite())
            .build();
    }

    private void validateSignUpRequest(String email, String nickname) {
        if (memberRepository.existsByEmail(email)) {
            throw new ServiceException(AuthError.DUPLICATED_EMAIL);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new ServiceException(AuthError.DUPLICATED_NICKNAME);
        }
    }
}
