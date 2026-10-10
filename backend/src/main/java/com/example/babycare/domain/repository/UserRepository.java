package com.example.babycare.domain.repository;

import com.example.babycare.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByOpenid(String openid);
    User save(User user);
    List<User> findAll();
}
