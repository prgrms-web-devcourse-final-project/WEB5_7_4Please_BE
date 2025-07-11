package com.deal4u.fourplease.domain.auth.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import com.deal4u.fourplease.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshToken extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(updatable = false, length = 500)
    private String token;

    @Column(updatable = false)
    private LocalDateTime expiryDate;

    public void updateToken(String newToken, LocalDateTime newExpiryDate) {
        this.token = newToken;
        this.expiryDate = newExpiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

}
