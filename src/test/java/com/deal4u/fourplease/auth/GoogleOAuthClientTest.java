package com.deal4u.fourplease.auth;

import com.deal4u.fourplease.domain.auth.client.GoogleOAuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleOAuthClientTest {
    @InjectMocks
    private GoogleOAuthClient googleOAuthClient;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleOAuthClient, "clientId", "test-client-id");
        ReflectionTestUtils.setField(googleOAuthClient, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(googleOAuthClient, "redirectUri", "http://localhost/callback");
    }

    @Test
    void getAccessToken() {
        //given
        String authCode = "4%2F0AVMBsJg05s6etuFs4v8KjoBWzAoAGPJjPL5qHoiAZbDRO0KvBG_RuL_2_cYePXHyTt94gA";
        String expectedToken  = "ya29.mocktoken";

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", expectedToken);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        //when
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class))
        ).thenReturn(responseEntity);

        //then
        String accessToken = googleOAuthClient.getAccessToken(authCode);

        assertEquals(expectedToken, accessToken);
    }

}
