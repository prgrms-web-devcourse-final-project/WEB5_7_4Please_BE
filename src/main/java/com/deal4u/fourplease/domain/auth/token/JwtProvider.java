package com.deal4u.fourplease.domain.auth.token;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {


    private final String secretKey;

    private final long accessTokenExpiration;

    private final long refreshTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                       @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
        this.secretKey = secretKey;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }


    public TokenPair generateTokenPair(Member member) {
        return new TokenPair(
                generateAccessToken(member),
                generateRefreshToken(member)
        );
    }

    private String generateAccessToken(Member member) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        log.info("멤버의 롤: " + member.getRole());

        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("role", member.getRole().name())
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(Member member) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        validateOrThrow(token);

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        validateOrThrow(token);

        String type = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);

        if (type == null) {
            throw ErrorCode.TOKEN_TYPE_NOT_FOUND.toException();
        }
        return type;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }


    public LocalDateTime getExpirationFromToken(String token) {
        validateOrThrow(token);

        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public void validateOrThrow(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw ErrorCode.TOKEN_EXPIRED.toException(e);
        } catch (MalformedJwtException e) {
            throw ErrorCode.MALFORMED_TOKEN.toException(e);
        } catch (UnsupportedJwtException e) {
            throw ErrorCode.UNSUPPORTED_TOKEN.toException(e);
        } catch (JwtException e) {
            throw ErrorCode.INVALID_ACCESS_TOKEN.toException(e);
        }
    }
}
