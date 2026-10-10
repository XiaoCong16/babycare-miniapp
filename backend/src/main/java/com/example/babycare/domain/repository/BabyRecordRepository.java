package com.example.babycare.domain.repository;

import com.example.babycare.domain.model.BabyRecord;

import java.util.List;
import java.util.Optional;

public interface BabyRecordRepository {
    BabyRecord save(BabyRecord record);
    List<BabyRecord> findByUserIdOrderByRecordTimeDesc(Long userId);
    BabyRecord findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(Long userId, String recordType);
}
