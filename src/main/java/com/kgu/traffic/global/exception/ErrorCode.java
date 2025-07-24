package com.kgu.traffic.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // user
    MEMBER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "로그인하지 않은 사용자입니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보가 존재하지 않습니다"),
    DUPLICATE_MEMBER_LOGIN_ID(HttpStatus.CONFLICT, "중복된 로그인 아이디입니다"),
    PROFILE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필 이미지를 찾을 수 없습니다"),
    MEMBER_NOT_ADMIN(HttpStatus.FORBIDDEN, "관리자가 아닙니다"),

    // info
    INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "정보를 찾을 수 없습니다."),


    // auth
    MEMBER_JOIN_REQUIRED(HttpStatus.MULTIPLE_CHOICES, "회원가입이 필요합니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다."),
    EXPIRED_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 로그인 토큰입니다."),
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "올바르지 않은 로그인 토큰입니다."),
    NOT_BEARER_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "Bearer 타입의 토큰이 아닙니다."),
    UNSUPPORTED_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 형식의 토큰입니다."),
    NEED_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다."),
    INCORRECT_PASSWORD_OR_ACCOUNT(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸거나, 해당 계정이 존재하지 않습니다."),
    INCORRECT_ACCOUNT(HttpStatus.UNAUTHORIZED, "해당 계정이 존재하지 않습니다."),
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다."),
    ACCOUNT_USERNAME_EXIST(HttpStatus.UNAUTHORIZED, "해당 계정이 존재합니다."),

    // others
    REQUEST_OK(HttpStatus.OK, "올바른 요청입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 요청입니다."),
    NOT_ENOUGH_PERMISSION(HttpStatus.FORBIDDEN, "해당 권한이 없습니다."),
    INTERNAL_SEVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생하였습니다. 관리자에게 문의해 주세요."),
    FOR_TEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "테스트용 에러입니다."),
    ENUM_CONVERSION_ERROR(HttpStatus.BAD_REQUEST, "Enum 변환 오류입니다."),

    // validation
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "이메일은 필수입니다."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호는 필수입니다."),
    PASSWORD_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영문과 숫자를 혼합하여 10자 이상이어야 합니다."),
    GENDER_REQUIRED(HttpStatus.BAD_REQUEST, "성별은 필수입니다."),
    PROFILE_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "프로필 이미지 선택은 필수입니다."),
    // alarm
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "약 알람 그룹을 찾을 수 없습니다."),

    // sns
    SMS_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "검증 코드가 일치하지 않습니다!"),
    SMS_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "휴대폰 인증이 되지 않았습니다."),

    // firebase
    FIREBASE_ENV_NOT_FOUND(HttpStatus.NOT_FOUND, "Firebase 환경변수를 찾을 수 없습니다."),
    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "fcm 토큰을 찾을 수 없습니다"),
    FCM_SEND_FAILED(HttpStatus.BAD_REQUEST, "보낼 수 없습니다"),

    //report
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "레포트를 찾을 수 없습니다"),

    //ADMIN
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "어드민을 찾을 수 없습니다"),

    //report
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "금지된 접근 입니다.");

    private final HttpStatus status;
    private final String message;

    // 메시지를 기반으로 ErrorCode를 찾는 정적 메서드
    public static ErrorCode fromMessage(String message) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getMessage().equals(message)) {
                return errorCode;
            }
        }
        throw TrafficException.from(ErrorCode.ENUM_CONVERSION_ERROR);
    }
}
