package com.deal4u.fourplease.domain.order.repository;

import com.deal4u.fourplease.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// Todo: 실제 레포지토리 변경 및 삭제 필요
public interface TempMemberRepository extends JpaRepository<Member, Long> {
}
