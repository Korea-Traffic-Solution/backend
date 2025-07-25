package com.kgu.traffic.global.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kgu.traffic.global.exception.ErrorCode;
import com.kgu.traffic.global.exception.TrafficException;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import com.kgu.traffic.global.domain.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private Status status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Metadata metadata;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<T> results;

    // 단일 결과 생성자
    public ApiResponse(T data) {
        this.status = new Status(ErrorCode.REQUEST_OK);
        this.metadata = new Metadata(1);
        this.results = List.of(data);
    }

    public ApiResponse(T data, ErrorCode errorCode) {
        this.results = List.of(data);
        this.status = new Status(errorCode);
    }

    // 리스트 결과 생성자
    public ApiResponse(List<T> results) {
        this.status = new Status(ErrorCode.REQUEST_OK);
        this.metadata = new Metadata(results.size());
        this.results = results;
    }

    // Page 타입 생성자
    public ApiResponse(Page<T> page) {
        this.status = new Status(ErrorCode.REQUEST_OK);
        this.metadata = new Metadata(page.getContent().size(), page.getPageable(), page.hasNext());
        this.results = page.getContent();
    }

    // Slice 타입 생성자
    public ApiResponse(Slice<T> slice) {
        this.status = new Status(ErrorCode.REQUEST_OK);
        this.metadata = new Metadata(slice.getContent().size(), slice.getPageable(), slice.hasNext());
        this.results = slice.getContent();
    }

    // 정상 응답 코드 생성자
    public ApiResponse(SuccessCode successCode) {
        this.status = new Status(successCode);
    }

    // 오류 코드 생성자
    public ApiResponse(ErrorCode errorCode) {
        this.status = new Status(errorCode);
    }

    // 커스텀 예외 생성자
    public ApiResponse(TrafficException exception) {
        this.status = new Status(exception.getErrorCode());
    }

    @Getter
    private static class Metadata {
        private int resultCount = 0;

        @JsonInclude(JsonInclude.Include.NON_NULL) // 값이 설정되지 않으면 JSON 응답에서 제외
        private Pageable pageable;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean hasNext;

        // 일반 리스트/단일 결과용 메타데이터 생성자
        public Metadata(int resultCount) {
            this.resultCount = resultCount;
        }

        // Page용 메타데이터 생성자
        public Metadata(int resultCount, Pageable pageable, boolean hasNext) {
            this.resultCount = resultCount;
            this.pageable = pageable;
            this.hasNext = hasNext;
        }
    }

    @Getter
    private static class Status {
        private int code;
        private String message;

        public Status(ErrorCode errorCode) {
            this.code = errorCode.getStatus().value();
            this.message = errorCode.getMessage();
        }

        public Status(SuccessCode successCode) {
            this.code = successCode.getStatus().value();
            this.message = successCode.getMessage();
        }
    }
}
