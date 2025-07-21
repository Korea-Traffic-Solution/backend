package com.kgu.traffic.global.exception;

import lombok.Getter;

@Getter
public class TrafficException extends RuntimeException {
    private final ErrorCode errorCode;
    private String message;

    private TrafficException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private TrafficException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public static TrafficException from(ErrorCode errorCode) {
        return new TrafficException(errorCode);
    }

    public static TrafficException from(ErrorCode errorCode, String message) {
        return new TrafficException(errorCode, message);
    }
}
