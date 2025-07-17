package com.deal4u.fourplease.domain.notification.pushnotification.controller;

import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.Receiver;
import com.deal4u.fourplease.domain.notification.pushnotification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class PushNotificationController {

    private final MemberRepository memberRepository;
    private final PushNotificationService pushNotificationService;

    @PatchMapping("/{notificationId}/clicked")
    void clickNotification(@PathVariable(name = "notificationId") Long id) {
        Member first = memberRepository.findAll().getFirst();
        pushNotificationService.click(Receiver.of(first.getMemberId()), id);
    }
}
