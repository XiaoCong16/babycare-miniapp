package com.example.babycare.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("baby_records")
public class BabyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String babyName = "宝宝";

    private String recordType;

    private LocalDateTime recordTime;

    private String feedType;

    private BigDecimal milkAmount;

    private String diaperStatus;

    private String diaperNote;

    private Integer sleepDuration;

    private BigDecimal temperature;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();

    public BabyRecord() {
    }
}
