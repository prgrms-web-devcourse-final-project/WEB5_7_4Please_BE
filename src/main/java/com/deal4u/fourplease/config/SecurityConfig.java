package com.deal4u.fourplease.config;

import com.deal4u.fourplease.domain.auth.filter.JwtAuthenticationFilter;
import com.deal4u.fourplease.domain.auth.handler.Oauth2AuthenticationSuccessHandler;
import com.deal4u.fourplease.domain.auth.service.CustomOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final Oauth2AuthenticationSuccessHandler oauth2AuthSuccessHandler;
    private final CustomOauth2UserService customOauth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SecurityFilterChain Bean 등록.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .oauth2Login(oauth2 ->
                        oauth2.authorizationEndpoint(
                                        authorization -> authorization.baseUri("/api/v1/login/page")
                                )
                                .redirectionEndpoint(
                                        redir -> redir.baseUri("/api/v1/login/{registrationId}"))
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOauth2UserService)
                                )
                                .successHandler(oauth2AuthSuccessHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/oauth2/**",
                                "/api/v1/login/**",
                                "/api/v1/signup/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }


}
