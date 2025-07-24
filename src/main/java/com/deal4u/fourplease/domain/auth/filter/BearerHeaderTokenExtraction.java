package com.deal4u.fourplease.domain.auth.filter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BearerHeaderTokenExtraction implements TokenExtraction {

    private static final String HEADER_NAME = "Authorization";
    private static final String PREFIX = "Bearer ";

    @Override
    public Optional<String> extract(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (header == null) {
            return Optional.empty();
        }
        if (!header.startsWith(PREFIX)) {
            return Optional.empty();
        }
        String token = header.substring(PREFIX.length());
        log.info("Authorization Header: {}", header);
        return Optional.of(token);
    }
}
