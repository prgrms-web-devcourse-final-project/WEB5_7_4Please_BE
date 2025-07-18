package com.deal4u.fourplease.domain.member.entity;


import com.deal4u.fourplease.domain.common.BaseDateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    @NaturalId
    private String email;

    @Setter
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private Role role;

    @Enumerated(EnumType.STRING)
    @Setter
    private Status status;

    @Column(nullable = false)
    private String provider;

    @Builder
    public Member(String email, String nickName, Role role, Status status,
                  String provider) {
        this.email = email;
        this.nickName = nickName;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : Status.ACTIVE;
        this.provider = provider;
    }
}
