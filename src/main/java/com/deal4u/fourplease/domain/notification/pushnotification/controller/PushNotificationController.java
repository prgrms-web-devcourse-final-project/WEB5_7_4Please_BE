package com.deal4u.fourplease.domain.notification.pushnotification.controller;

import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.member.repository.MemberRepository;
import com.deal4u.fourplease.domain.notification.NotificationSender;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationListResponse;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationPageRequest;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationResponse;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.Receiver;
import com.deal4u.fourplease.domain.notification.pushnotification.message.PushNotificationMessage;
import com.deal4u.fourplease.domain.notification.pushnotification.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class PushNotificationController {

    private final MemberRepository memberRepository;
    private final PushNotificationService pushNotificationService;
    private final NotificationSender notificationSender;

    @Operation(summary = "클릭 시 발생하는 알림")
    @ApiResponse(responseCode = "200", description = "알림 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
    @PatchMapping("/click/{notificationId}")
    void clickNotification(@PathVariable(name = "notificationId") Long id) {
        Member first = memberRepository.findAll().getFirst();
        pushNotificationService.click(Receiver.of(first.getMemberId()), id);
    }

    @Operation(summary = "상단 배너의 [알림] 버튼 클릭 시 5개씩 보여주는 알림")
    @ApiResponse(responseCode = "200", description = "알림 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
    @GetMapping("/list")
    PushNotificationListResponse listPushNotification() {
        Member first = memberRepository.findAll().getFirst();
        return pushNotificationService.getList(Receiver.of(first.getMemberId()));
    }

    @Operation(summary = "전체 알림 목록")
    @ApiResponse(responseCode = "200", description = "알림 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
    @GetMapping("/view")
    Slice<PushNotificationResponse> viewPushNotification(
            @Valid PushNotificationPageRequest pageRequest) {
        Member first = memberRepository.findAll().getFirst();
        return pushNotificationService.getView(Receiver.of(first.getMemberId()),
                pageRequest.toPageable());
    }

    //이건 테스트용입니다
    @PostMapping("/push/{message}")
    @Transactional
    void listPushNotification(@PathVariable String message) {
        PushNotificationMessage message1 = PushNotificationMessage.urlMessageBuilder()
                .addReceiver(1L)
                .type("CreateUser")
                .url("http://난유한님믿어.com")
                .message(message)
                .build();
        notificationSender.send(message1);
    }
}
