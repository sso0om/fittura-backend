package com.fittura.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String ROLES_CLAIM_KEY = "roles";

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final JwtParser jwtParser;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds
    ) {
        // Base64로 인코딩된 secretKey를 디코딩하여 byte 배열로 변환
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // HMAC-SHA 알고리즘에 사용할 키를 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;

        // 생성된 키를 사용하여 JWT 파서를 빌드
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }

    public String generateAccessToken(String memberId, Set<String> roles) {
        return Jwts.builder()
            .subject(memberId)
            .claim(ROLES_CLAIM_KEY, roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenValidityInMilliseconds))
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(String memberId) {
        return Jwts.builder()
            .subject(memberId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenValidityInMilliseconds))
            .signWith(key)
            .compact();
    }

    public Claims getClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰이 비어있습니다.");
        }

        try {
            // 서명 검증, 토큰 검증 + 파싱
            return jwtParser
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우, 예외 객체에서 Claims를 추출하여 반환
            return e.getClaims();
        }
    }

    public TokenStatus validateToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenStatus.INVALID;
        }

        try {
            jwtParser.parseSignedClaims(token); // 서명 검증, 토큰 검증 + 파싱
            return TokenStatus.VALID;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다. {}", e.getMessage());
            return TokenStatus.INVALID;
        } catch (ExpiredJwtException e) { // 만료된 JWT 토큰
            return TokenStatus.EXPIRED;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다. {}", e.getMessage());
            return TokenStatus.UNSUPPORTED;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다. {}", e.getMessage());
            return TokenStatus.INVALID;
        }
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = getClaims(accessToken);

        if (claims.get(ROLES_CLAIM_KEY) == null) {
            throw new UnsupportedJwtException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities = ((Collection<String>) claims.get(ROLES_CLAIM_KEY, Collection.class))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 인증된 사용자
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
