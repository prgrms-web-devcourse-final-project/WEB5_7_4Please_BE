package com.deal4u.fourplease.auth.token;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.deal4u.fourplease.domain.auth.dto.TokenPair;
import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Role;
import com.deal4u.fourplease.global.exception.ErrorCode;
import com.deal4u.fourplease.global.exception.GlobalException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtProviderTest {
    private JwtProvider jwtProvider;
    private Member member;

    @BeforeEach
    void setUp() {
        SecretKey strongKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String encodedSecret = Base64.getEncoder().encodeToString(strongKey.getEncoded());

        jwtProvider = new JwtProvider(encodedSecret, 1000L * 60 * 15, 1000L * 60 * 60 * 24 * 7);

        member = Member.builder()
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("토큰 생성 시 Access/Refresh 토큰 모두 발급된다")
    void generateTokenPair_shouldReturnBothTokens() {
        TokenPair pair = jwtProvider.generateTokenPair(member);

        assertThat(pair).isNotNull();
        assertThat(pair.accessToken()).isNotBlank();
        assertThat(pair.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("AccessToken에서 이메일을 정확히 파싱할 수 있다")
    void getEmailFromToken_shouldExtractCorrectEmail() {
        String accessToken = jwtProvider.generateTokenPair(member).accessToken();

        String email = jwtProvider.getEmailFromToken(accessToken);

        assertThat(email).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("AccessToken에서 타입이 access로 설정되어 있다")
    void getTokenType_shouldReturnAccess() {
        String accessToken = jwtProvider.generateTokenPair(member).accessToken();

        String type = jwtProvider.getTokenType(accessToken);

        assertThat(type).isEqualTo("access");
    }

    @Test
    @DisplayName("올바른 토큰은 validateToken에서 true를 반환한다")
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtProvider.generateTokenPair(member).accessToken();

        boolean result = jwtProvider.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("잘못된 시크릿 키로 만든 토큰은 validateToken에서 false를 반환한다")
    void validateToken_shouldReturnFalseForInvalidToken() {
        // 다른 키로 직접 만든 잘못된 토큰
        byte[] otherKeyBytes =
                (
                        "this-is-a-super-secure-and-very-very-very-long-secret-key-"
                                + "used-for-testing-hs512-algorithm-purposes-only-please-"
                                + "make-it-safe-and-strong-just-in-case!"
                )
                        .getBytes(StandardCharsets.UTF_8);
        SecretKey otherKey = Keys.hmacShaKeyFor(otherKeyBytes);


        String badToken = Jwts.builder()
                .setSubject(member.getEmail())
                .signWith(otherKey, SignatureAlgorithm.HS512)
                .compact();

        boolean result = jwtProvider.validateToken(badToken);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 TOKEN_EXPIRED 예외를 던진다")
    void validateOrThrow_shouldThrowExceptionForExpiredToken() throws InterruptedException {
        // 만료시간을 아주 짧게 설정해 테스트용 JwtProvider 생성
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 1L);
        String token = jwtProvider.generateTokenPair(member).accessToken();

        Thread.sleep(10); // 만료 유도

        assertThatThrownBy(() -> jwtProvider.validateOrThrow(token))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("type이 없는 토큰은 TOKEN_TYPE_NOT_FOUND 예외 발생")
    void getTokenType_shouldThrowIfNoTypeClaim() {

        String encodedSecret = (String) ReflectionTestUtils.getField(jwtProvider, "secretKey");
        byte[] keyBytes = Decoders.BASE64.decode(encodedSecret);
        Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());

        // type 없이 토큰 생성
        String token = Jwts.builder()
                .setSubject(member.getEmail())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        assertThatThrownBy(() -> jwtProvider.getTokenType(token))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ErrorCode.TOKEN_TYPE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getExpirationFromToken은 만료 시간을 정확히 추출한다")
    void getExpirationFromToken_shouldReturnExpirationTime() {
        String token = jwtProvider.generateTokenPair(member).accessToken();

        LocalDateTime expiration = jwtProvider.getExpirationFromToken(token);

        assertThat(expiration).isAfter(LocalDateTime.now());
    }

}
