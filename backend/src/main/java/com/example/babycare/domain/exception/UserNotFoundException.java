package com.example.babycare.domain.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super("用户不存在: " + userId);
    }
}
