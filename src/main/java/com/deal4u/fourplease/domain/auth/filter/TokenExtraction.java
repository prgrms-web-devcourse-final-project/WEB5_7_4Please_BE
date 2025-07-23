package com.deal4u.fourplease.domain.auth.filter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface TokenExtraction {

    Optional<String> extract(HttpServletRequest request);
}
