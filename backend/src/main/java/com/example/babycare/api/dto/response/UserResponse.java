package com.example.babycare.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String openid;
    private String nickName;
    private String avatarUrl;
}
