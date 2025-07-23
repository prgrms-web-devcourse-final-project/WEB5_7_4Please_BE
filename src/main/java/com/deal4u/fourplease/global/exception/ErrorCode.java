package com.deal4u.fourplease.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 - Bad Request,
    EMPTY_LIST(HttpStatus.BAD_REQUEST, "빈 리스트 입니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "처리할수 없는 파일입니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리 할 수 없습니다."),
    INVALID_AUCTION_BIDDER(HttpStatus.BAD_REQUEST, "해당 사용자는 경매의 낙찰자가 아닙니다."),
    INVALID_ORDER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 타입입니다."),
    INVALID_INSTANT_BID_PRICE(HttpStatus.BAD_REQUEST, "요청된 가격이 즉시 입찰가와 일치하지 않습니다."),
    INVALID_BID_PRICE(HttpStatus.BAD_REQUEST, "요청된 가격이 낙찰가와 일치하지 않습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    INVALID_USER(HttpStatus.BAD_REQUEST, "결제자 정보가 올바르지 않습니다."),
    PAYMENT_CONFIRMATION_FAILED(HttpStatus.BAD_REQUEST, "결제 승인이 실패했습니다."),
    PAYMENT_ERROR(HttpStatus.BAD_REQUEST, "결제 처리 중 오류가 발생했습니다."),
    WEBSOCKET_INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 요청 형식입니다."),
    INVALID_PRICE_NOT_UPPER(HttpStatus.BAD_REQUEST, "즉시 구매 가격이 현재 최고 입찰가보다 낮습니다."),
    PAYMENT_NOT_SUCCESS(HttpStatus.BAD_REQUEST, "결제 처리가 완료되지 않은 결제입니다."),
    INVALID_REVIEW_SORT(HttpStatus.BAD_REQUEST, "리뷰는 작성일자로만 정렬 가능합니다."),

    // 403 - Forbidden
    BID_FORBIDDEN_PRICE(HttpStatus.FORBIDDEN, "기존 입찰 금액보다 높은 금액을 입력해주세요."),
    AUCTION_NOT_OPEN(HttpStatus.FORBIDDEN, "해당 경매는 종료되었습니다."),
    AUCTION_NOT_CLOSED(HttpStatus.FORBIDDEN, "해당 경매는 종료되지 않았습니다."),
    FORBIDDEN_RECEIVER(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 404 - Not Found
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 경매를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정산을 찾을 수 없습니다."),
    BID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 입찰 내역을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    SECOND_HIGHEST_BIDDER_NOT_FOUND(HttpStatus.NOT_FOUND, "차상위 입찰자를 찾을 수 없습니다."),
    AUCTION_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 마감 스케쥴을 찾을 수 없습니다."),

    BID_PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "경매 기간을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "일치하는 상태를 찾을 수 없습니다."),
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 위시리스트를 찾을 수 없습니다."),
    SHIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 배송 정보를 찾을 수 없습니다."),

    // 409 - Conflict
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 처리된 결제입니다"),
    SETTLEMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 차상위 입찰자에 대한 정산이 존재합니다."),
    AUCTION_SCHEDULE_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 경매의 마감 스케쥴이 이미 등록되어 있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 리뷰가 작성되었습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "사용할 수 없는 타입입니다"),

    // 500 - Internal Server Error
    WEBSOCKET_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WebSocket 메시지 전송 중 오류가 발생하였습니다."),
    FAILED_SETTLEMENT_SCHEDULE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "실패한 정산 작업을 스케줄링하는 중 오류가 발생했습니다."),
    FAILED_SETTLEMENT_SCHEDULE_CANCEL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "실패한 정산 작업을 취소하는 중 오류가 발생했습니다."),
    EMAIL_SEND_FAILED_TO_SECOND_BIDDER(HttpStatus.INTERNAL_SERVER_ERROR,
            "2순위 입찰자에게 이메일을 전송하는 데 실패했습니다."),
    FAILED_SETTLEMENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "예약된 실패한 정산 작업이 존재하지 않습니다."),
    PUSH_NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "차상위 입찰자에게 푸시 알림을 전송하는 데 실패했습니다."),

    DOES_MODIFIED_URL(HttpStatus.INTERNAL_SERVER_ERROR, "알수없는 에러입니다.");

    private final HttpStatus status;
    private final String message;

    public GlobalException toException() {
        return new GlobalException(status, message);
    }

    public GlobalException toException(String message) {
        return new GlobalException(status, message);
    }

    public GlobalException toException(String message, Object... args) {
        return new GlobalException(status, message, args);
    }

    public GlobalException toException(Throwable cause) {
        return new GlobalException(cause, status, message);
    }

    public GlobalException toException(Throwable cause, String message) {
        return new GlobalException(cause, status, message);
    }

    public GlobalException toException(Throwable cause, String message, Object... args) {
        return new GlobalException(cause, status, message, args);
    }
}
