package com.deal4u.fourplease.domain.auth.filter;

import com.deal4u.fourplease.domain.member.entity.Member;
import java.util.Optional;

public interface AuthMemberReader {

    Optional<Member> findByEmail(String email);
}
