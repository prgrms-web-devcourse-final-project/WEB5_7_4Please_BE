package com.deal4u.fourplease.domain.auth.token;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.service.MemberService;
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

@Component
@Slf4j
public class JwtProvider {
    private final MemberService memberService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public JwtProvider(MemberService memberService) {
        this.memberService = memberService;
    }

    public TokenPair generateTokenPair(Member member) {
        memberService.validateMember(member);
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

        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        validateOrThrow(token);

        String type = Jwts.parser()
                .setSigningKey(getSigningKey())
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

        Date expiration = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public void validateOrThrow(String token) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token);
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
