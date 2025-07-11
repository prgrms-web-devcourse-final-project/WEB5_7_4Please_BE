package com.deal4u.fourplease.domain.member.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    @NaturalId
    private String email;

    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private String provider;

    @Builder
    public Member(String email, String nickName, Role role, Status status, String provider) {
        this.email = email;
        this.nickName = nickName;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : Status.ACTIVE;
        this.provider = provider;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}


