package com.deal4u.fourplease.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Health Check")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서버가 정상적으로 동작 중입니다.")
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
