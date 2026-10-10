package com.example.babycare.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String openid;

    private String nickName;
    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected User() {
    }

    public User(String openid, String nickName, String avatarUrl) {
        this.openid = openid;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.createdAt = LocalDateTime.now();
    }
}
