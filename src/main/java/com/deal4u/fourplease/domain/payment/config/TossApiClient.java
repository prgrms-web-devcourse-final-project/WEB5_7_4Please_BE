package com.deal4u.fourplease.domain.payment.config;


import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmRequest;
import com.deal4u.fourplease.domain.payment.dto.TossPaymentConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "tossClient",
        url = "https://api.tosspayments.com",
        configuration = TossFeignConfig.class
)
public interface TossApiClient {

    @PostMapping("/v1/payments/confirm")
    TossPaymentConfirmResponse confirmPayment(
            @RequestBody
            TossPaymentConfirmRequest request
    );
}
