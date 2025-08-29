package com.fittura.domain.member.service;

import com.fittura.domain.member.dto.SignUpResultDto;
import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.AuthError;
import com.fittura.domain.member.repository.MemberRepository;
import com.fittura.global.config.AppProperties;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final TokenService tokenService;

    @Transactional
    public SignUpResultDto signUp(SignUpReqDto req) {
        validateSignUpRequest(req);

        Member member = Member.createUser(
            req.email(),
            req.name(),
            req.nickname(),
            passwordEncoder.encode(req.password())
        );
        Member savedMember = memberRepository.save(member);

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
            .path(cookieProps.path())
            .maxAge(Duration.ofMillis(tokenDto.refreshTokenExpiresInMillis()))
            .sameSite(cookieProps.sameSite())
            .build();
    }

    private void validateSignUpRequest(SignUpReqDto req) {
        boolean isExistEmail = memberRepository.existsByEmail(req.email());
        if (isExistEmail) {
            throw new ServiceException(AuthError.DUPLICATED_EMAIL);
        }

        boolean isExistNickname = memberRepository.existsByNickname(req.nickname());
        if (isExistNickname) {
            throw new ServiceException(AuthError.DUPLICATED_NICKNAME);
        }
    }
}
