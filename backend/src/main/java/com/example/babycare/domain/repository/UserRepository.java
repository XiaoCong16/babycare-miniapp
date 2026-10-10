package com.example.babycare.domain.repository;

import com.example.babycare.domain.model.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository {
    Optional<User> findByOpenid(String openid);
    User save(User user);
    List<User> findAll();
}
