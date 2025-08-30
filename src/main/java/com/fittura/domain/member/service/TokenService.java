package com.fittura.domain.member.service;

import com.fittura.domain.member.dto.TokenDto;
import com.fittura.domain.member.entity.Member;
import com.fittura.domain.member.error.MemberError;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.security.JwtTokenProvider;
import com.fittura.global.security.TokenStatus;
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

    public String findSubjectByRefreshToken(String refreshToken) {
        TokenStatus tokenStatus = jwtTokenProvider.validateToken(refreshToken);
        if (tokenStatus != TokenStatus.VALID) {
            throw new ServiceException(MemberError.INVALID_REFRESH_TOKEN);
        }
        return jwtTokenProvider.getSubject(refreshToken);
    }


    // ========== 헬퍼 메서드 ==========

    private String generateAccessToken(Member savedMember, String memberId) {
        Set<String> roles = savedMember.getRoles()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());
        return jwtTokenProvider.generateAccessToken(memberId, roles);
    }
}
