package com.deal4u.fourplease.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blacklistedTokenId;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder
    public BlacklistedToken(String token, LocalDateTime expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
