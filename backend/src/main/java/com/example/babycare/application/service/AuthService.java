package com.example.babycare.application.service;

import com.example.babycare.api.dto.response.UserResponse;
import com.example.babycare.domain.exception.UserNotFoundException;
import com.example.babycare.domain.model.ReminderConfig;
import com.example.babycare.domain.model.User;
import com.example.babycare.domain.repository.ReminderConfigRepository;
import com.example.babycare.domain.repository.UserRepository;
import com.example.babycare.infrastructure.wechat.WeChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ReminderConfigRepository reminderConfigRepository;
    private final WeChatClient weChatClient;

    public AuthService(UserRepository userRepository,
                       ReminderConfigRepository reminderConfigRepository,
                       WeChatClient weChatClient) {
        this.userRepository = userRepository;
        this.reminderConfigRepository = reminderConfigRepository;
        this.weChatClient = weChatClient;
    }

    @Transactional
    public UserResponse login(String code, String nickName, String avatarUrl) {
        String openid = weChatClient.getOpenidByCode(code);

        User user = userRepository.findByOpenid(openid)
                .orElseGet(() -> userRepository.save(new User(openid, nickName, avatarUrl)));

        reminderConfigRepository.findByUserId(user.getId())
                .orElseGet(() -> reminderConfigRepository.save(ReminderConfig.defaultConfig(user.getId())));

        return new UserResponse(user.getId(), user.getOpenid(), user.getNickName(), user.getAvatarUrl());
    }
}
