package com.deal4u.fourplease.domain.auth.filter;

import com.deal4u.fourplease.domain.auth.token.JwtProvider;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.replace("Bearer ", "");
            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmailFromToken(token);
                Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자 없음"));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        member, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Authorization Header: {}", header);
                log.info("추출한 토큰: {}", token);
                log.info("토큰에서 추출한 이메일: {}", email);
                log.info("SecurityContext 설정됨: {}", auth.getName());
            }

        }

        chain.doFilter(request, response);
    }

}
