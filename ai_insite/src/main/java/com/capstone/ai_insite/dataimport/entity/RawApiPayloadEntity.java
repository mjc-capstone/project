package com.capstone.ai_insite.dataimport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "raw_api_payloads")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawApiPayloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "request_url", nullable = false, columnDefinition = "text")
    private String requestUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_params_json", columnDefinition = "json")
    private String requestParamsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body_json", nullable = false, columnDefinition = "json")
    private String responseBodyJson;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    public RawApiPayloadEntity(
        String sourceName,
        String serviceName,
        String requestUrl,
        String requestParamsJson,
        String responseBodyJson,
        Integer rowCount,
        String status
    ) {
        this.sourceName = sourceName;
        this.serviceName = serviceName;
        this.requestUrl = requestUrl;
        this.requestParamsJson = requestParamsJson;
        this.responseBodyJson = responseBodyJson;
        this.fetchedAt = LocalDateTime.now();
        this.rowCount = rowCount;
        this.status = status;
    }
}
