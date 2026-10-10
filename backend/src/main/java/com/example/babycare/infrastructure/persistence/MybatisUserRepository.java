package com.example.babycare.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.babycare.domain.model.User;
import com.example.babycare.domain.repository.UserRepository;
import com.example.babycare.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisUserRepository implements UserRepository {

    private final UserMapper mapper;

    public MybatisUserRepository(UserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByOpenid(String openid) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid)));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            mapper.insert(user);
        } else {
            mapper.updateById(user);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return mapper.selectList(null);
    }
}
