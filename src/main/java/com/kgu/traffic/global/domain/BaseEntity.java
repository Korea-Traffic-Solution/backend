package com.kgu.traffic.global.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EntityListeners(
        AuditingEntityListener
                .class) // 스프링 데이터 JPA의 감사(Auditing) 기능을 활성화하여, 엔티티가 생성되거나 업데이트될 때 특정 필드를 자동으로 설정
@Getter
@SuperBuilder // 하위 클래스에서도 빌더 패턴을 사용하여 객체를 생성할 수 있도록 지원
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@MappedSuperclass // 이 클래스가 엔티티가 아니며, 상속받는 엔티티 클래스에 공통 속성을 제공하는 기본 클래스임을 의미
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}