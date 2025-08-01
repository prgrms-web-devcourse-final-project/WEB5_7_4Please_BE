package com.deal4u.fourplease.domain.member.repository;

import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.entity.Status;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndProvider(String email, String provider);

    boolean existsByNickName(String nickName);

    Optional<Member> findByMemberIdAndStatus(Long memberId, Status status);
}
