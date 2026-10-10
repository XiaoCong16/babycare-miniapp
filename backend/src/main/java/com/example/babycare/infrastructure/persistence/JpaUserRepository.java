package com.example.babycare.infrastructure.persistence;

import com.example.babycare.domain.model.User;
import com.example.babycare.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository delegate;

    public JpaUserRepository(SpringDataUserRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<User> findByOpenid(String openid) {
        return delegate.findByOpenid(openid);
    }

    @Override
    public User save(User user) {
        return delegate.save(user);
    }

    @Override
    public List<User> findAll() {
        return delegate.findAll();
    }
}
