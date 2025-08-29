package com.fittura.domain.member.service;

import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.entity.Member;
import com.fittura.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenDto issueTokens(Member savedMember) {
        String memberId = savedMember.getId().toString();
        String accessToken = generateAccessToken(savedMember, memberId);

        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        long refreshTokenExpiresInMillis = jwtTokenProvider.getRefreshTokenValidityInMilliseconds();

        return new TokenDto(accessToken, refreshToken, refreshTokenExpiresInMillis);
    }

    private String generateAccessToken(Member savedMember, String memberId) {
        Set<String> roles = savedMember.getRoles()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());
        return jwtTokenProvider.generateAccessToken(memberId, roles);
    }
}
