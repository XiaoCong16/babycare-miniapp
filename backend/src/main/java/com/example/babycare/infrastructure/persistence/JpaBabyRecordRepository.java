package com.example.babycare.infrastructure.persistence;

import com.example.babycare.domain.model.BabyRecord;
import com.example.babycare.domain.repository.BabyRecordRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaBabyRecordRepository implements BabyRecordRepository {

    private final SpringDataBabyRecordRepository delegate;

    public JpaBabyRecordRepository(SpringDataBabyRecordRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public BabyRecord save(BabyRecord record) {
        return delegate.save(record);
    }

    @Override
    public List<BabyRecord> findByUserIdOrderByRecordTimeDesc(Long userId) {
        return delegate.findByUserIdOrderByRecordTimeDesc(userId);
    }

    @Override
    public BabyRecord findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(Long userId, String recordType) {
        return delegate.findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(userId, recordType);
    }
}
