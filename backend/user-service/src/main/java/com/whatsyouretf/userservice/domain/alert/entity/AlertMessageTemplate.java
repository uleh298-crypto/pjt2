package com.whatsyouretf.userservice.domain.alert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 메시지 템플릿 엔티티
 * <p>
 * 알림 유형별 메시지 템플릿을 관리합니다.
 * 변수 치환: {etf_name}, {news_title} 등
 */
@Entity
@Table(name = "alert_message_template", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alert_template_version", columnNames = {"alert_type_code", "version"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlertMessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 유형 코드 (alert_type FK) */
    @Column(name = "alert_type_code", nullable = false, length = 50)
    private String alertTypeCode;

    /** 버전 (v1.0, v1.1 등) */
    @Column(nullable = false, length = 20)
    private String version;

    /** 제목 템플릿 */
    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    /** 메시지 템플릿 */
    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    /** 사용 가능한 변수 목록 (JSON 배열) */
    @Column(columnDefinition = "TEXT")
    private String variables;

    /** 설명 */
    @Column(length = 200)
    private String description;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 템플릿 변수 치환 (다중 변수)
     * @param variables 변수명-값 맵
     * @return 치환된 제목
     */
    public String renderTitle(java.util.Map<String, String> variables) {
        String result = titleTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * 템플릿 변수 치환 (다중 변수)
     * @param variables 변수명-값 맵
     * @return 치환된 메시지
     */
    public String renderMessage(java.util.Map<String, String> variables) {
        String result = messageTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
