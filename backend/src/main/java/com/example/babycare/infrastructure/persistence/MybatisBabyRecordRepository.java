package com.example.babycare.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.babycare.domain.model.BabyRecord;
import com.example.babycare.domain.repository.BabyRecordRepository;
import com.example.babycare.infrastructure.persistence.mapper.BabyRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisBabyRecordRepository implements BabyRecordRepository {

    private final BabyRecordMapper mapper;

    public MybatisBabyRecordRepository(BabyRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BabyRecord save(BabyRecord record) {
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.updateById(record);
        }
        return record;
    }

    @Override
    public List<BabyRecord> findByUserIdOrderByRecordTimeDesc(Long userId) {
        return mapper.selectList(new LambdaQueryWrapper<BabyRecord>()
                .eq(BabyRecord::getUserId, userId)
                .orderByDesc(BabyRecord::getRecordTime));
    }

    @Override
    public BabyRecord findTopByUserIdAndRecordTypeOrderByRecordTimeDesc(Long userId, String recordType) {
        return mapper.selectOne(new LambdaQueryWrapper<BabyRecord>()
                .eq(BabyRecord::getUserId, userId)
                .eq(BabyRecord::getRecordType, recordType)
                .orderByDesc(BabyRecord::getRecordTime)
                .last("LIMIT 1"));
    }
}
