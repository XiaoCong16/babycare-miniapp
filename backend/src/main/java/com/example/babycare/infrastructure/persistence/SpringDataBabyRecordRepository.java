package com.example.babycare.infrastructure.persistence;

import com.example.babycare.domain.model.BabyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBabyRecordRepository extends JpaRepository<BabyRecord, Long> {
    List<BabyRecord> findByUserIdOrderByRecordTimeDesc(Long userId);
    BabyRecord findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(Long userId, String recordType);
}
