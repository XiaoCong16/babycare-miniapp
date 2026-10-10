package com.example.babycare.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "baby_records")
public class BabyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "baby_name")
    private String babyName = "宝宝";

    @Column(name = "record_type", nullable = false)
    private String recordType;

    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;

    @Column(name = "feed_type")
    private String feedType;

    @Column(name = "milk_amount", precision = 5, scale = 1)
    private BigDecimal milkAmount;

    @Column(name = "diaper_status")
    private String diaperStatus;

    @Column(name = "diaper_note")
    private String diaperNote;

    @Column(name = "sleep_duration")
    private Integer sleepDuration;

    @Column(name = "temperature", precision = 4, scale = 2)
    private BigDecimal temperature;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    protected BabyRecord() {
    }
}
