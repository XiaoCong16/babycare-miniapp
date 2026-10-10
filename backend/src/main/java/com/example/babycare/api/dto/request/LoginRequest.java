package com.example.babycare.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoginRequest {
    @NotBlank(message = "code 不能为空")
    private String code;

    private String nickName;
    private String avatarUrl;
}

