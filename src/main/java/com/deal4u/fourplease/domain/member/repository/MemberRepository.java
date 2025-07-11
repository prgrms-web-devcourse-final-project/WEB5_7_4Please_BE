package com.deal4u.fourplease.domain.member.repository;

import com.deal4u.fourplease.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
