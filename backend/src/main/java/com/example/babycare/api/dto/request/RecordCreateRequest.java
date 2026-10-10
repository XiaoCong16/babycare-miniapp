package com.example.babycare.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecordCreateRequest {
    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotBlank(message = "type 不能为空")
    private String type;

    @NotNull(message = "recordTime 不能为空")
    private LocalDateTime recordTime;

    private String feedType;
    private BigDecimal milkAmount;
    private String diaperStatus;
    private String diaperNote;
    private Integer sleepDuration;
    private BigDecimal temperature;
    private String note;
}
