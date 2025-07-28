package com.deal4u.fourplease.config;

import com.deal4u.fourplease.domain.auth.filter.JwtAuthenticationFilter;
import com.deal4u.fourplease.domain.auth.handler.Oauth2AuthenticationSuccessHandler;
import com.deal4u.fourplease.domain.auth.service.CustomOauth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정 클래스.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final Oauth2AuthenticationSuccessHandler oauth2AuthSuccessHandler;
    private final CustomOauth2UserService customOauth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ClientRegistrationRepository clientRegistrationRepository;

    /**
     * SecurityFilterChain Bean 등록.
     */

    @Bean
    @Order(1)
    public SecurityFilterChain authFileChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(
                        "/api/v1/auth/**",
                        "/api/v1/my/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        auth ->
                                auth.anyRequest().authenticated()
                ).exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(
                                (request, response, authException) -> response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED))
                ).addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                ).build();

    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2Login(oauth2 ->
                        oauth2.authorizationEndpoint(
                                        authorization -> authorization.baseUri("/api/v1/login/page")
                                                .authorizationRequestRepository(
                                                        new CustomAuthorizationRequestRepository())
                                )

                                .redirectionEndpoint(
                                        redir -> redir.baseUri("/api/v1/login/{registrationId}"))
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOauth2UserService)
                                )
                                .successHandler(oauth2AuthSuccessHandler)
                ).authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/oauth2/**",
                                "/api/v1/login/**",
                                "/api/v1/signup/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().authenticated()
                ).exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(
                                        (request, response, authException) -> response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public class CustomAuthorizationRequestRepository implements
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

        private final DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/api/v1/login"
                );

        @Override
        public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
            return null;
        }

        @Override
        public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {

        }

        @Override
        public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                     HttpServletResponse response) {
            log.info("removeAuthorizationRequest");
            OAuth2AuthorizationRequest resolve = resolver.resolve(request);
            return OAuth2AuthorizationRequest.from(resolve).state(request.getParameter("state"))
                    .build();
        }
    }
}
