package com.deal4u.fourplease.domain.notification.pushnotification.controller;

import com.deal4u.fourplease.domain.common.PageResponse;
import com.deal4u.fourplease.domain.member.entity.Member;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationListResponse;
import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationPageRequest;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.PushNotification;
import com.deal4u.fourplease.domain.notification.pushnotification.entity.Receiver;
import com.deal4u.fourplease.domain.notification.pushnotification.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    @Operation(summary = "클릭 시 발생하는 알림")
    @ApiResponse(responseCode = "200", description = "알림 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
    @PatchMapping("/click/{notificationId}")
    void clickNotification(@PathVariable(name = "notificationId") Long id,
            @AuthenticationPrincipal Member member) {
        pushNotificationService.click(Receiver.of(member.getMemberId()), id);
    }

    @Operation(summary = "상단 배너의 [알림] 버튼 클릭 시 5개씩 보여주는 알림")
    @ApiResponse(responseCode = "200", description = "알림 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
    @GetMapping("/list")
    PushNotificationListResponse listPushNotification(@AuthenticationPrincipal Member member) {
        return pushNotificationService.getList(Receiver.of(member.getMemberId()));
    }

    @Operation(summary = "전체 알림 목록")
    @ApiResponse(responseCode = "200", description = "알림 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
    @GetMapping("/view")
    PageResponse<PushNotification> viewPushNotification(
            @Valid PushNotificationPageRequest pageRequest,
            @AuthenticationPrincipal Member member) {
        return pushNotificationService.getView(Receiver.of(member.getMemberId()),
                pageRequest.toPageable());
    }
}
