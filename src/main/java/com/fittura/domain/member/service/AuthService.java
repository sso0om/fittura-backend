package com.fittura.domain.member.service;

import com.fittura.domain.member.dto.SignUpResultDto;
import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.dto.request.SignUpReqDto;
import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.AuthError;
import com.fittura.domain.member.repository.MemberRepository;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResultDto signUp(@Valid SignUpReqDto req) {
        validateSignUpRequest(req);

        Member member = Member.createUser(
            req.email(),
            req.name(),
            req.nickname(),
            passwordEncoder.encode(req.password())
        );
        Member savedMember = memberRepository.save(member);

        TokenDto tokenDto = generateTokens(savedMember);
        return new SignUpResultDto(savedMember, tokenDto);
    }

    private TokenDto generateTokens(Member savedMember) {
        String memberId = savedMember.getId().toString();
        String accessToken = generateAccessToken(savedMember, memberId);

        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        long refreshTokenExpirationTime = jwtTokenProvider.getRefreshTokenValidityInMilliseconds();

        return new TokenDto(accessToken, refreshToken, refreshTokenExpirationTime);
    }

    private String generateAccessToken(Member savedMember, String memberId) {
        Set<String> roles = savedMember.getRoles()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toSet());
        return jwtTokenProvider.generateAccessToken(memberId, roles);
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
