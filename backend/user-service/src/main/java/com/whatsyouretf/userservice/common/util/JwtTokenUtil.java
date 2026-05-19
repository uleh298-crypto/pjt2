package com.whatsyouretf.userservice.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {

    private final String secret;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity
    ) {
        this.secret = secret;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidity))
                .sign(Algorithm.HMAC512(secret));
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .sign(Algorithm.HMAC512(secret));
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        return Long.parseLong(jwt.getSubject());
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            decodeToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Refresh Token인지 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            String type = jwt.getClaim("type").asString();
            return "refresh".equals(type);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 토큰 만료 시간 조회
     */
    public Date getExpirationFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        return jwt.getExpiresAt();
    }

    /**
     * Authorization 헤더에서 토큰 추출
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private DecodedJWT decodeToken(String token) {
        return JWT.require(Algorithm.HMAC512(secret))
                .build()
                .verify(token);
    }
}
