package com.example.babycare.application.service;

import com.example.babycare.api.dto.request.RecordCreateRequest;
import com.example.babycare.domain.exception.InvalidRecordTypeException;
import com.example.babycare.domain.model.BabyRecord;
import com.example.babycare.domain.repository.BabyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecordService {

    private final BabyRecordRepository babyRecordRepository;

    public RecordService(BabyRecordRepository babyRecordRepository) {
        this.babyRecordRepository = babyRecordRepository;
    }

    @Transactional
    public BabyRecord createRecord(RecordCreateRequest request) {
        validate(request);

        BabyRecord record = new BabyRecord();
        record.setUserId(request.getUserId());
        record.setRecordType(request.getType());
        record.setBabyName("宝宝");
        record.setRecordTime(request.getRecordTime());
        record.setFeedType(request.getFeedType());
        record.setMilkAmount(request.getMilkAmount());
        record.setDiaperStatus(request.getDiaperStatus());
        record.setDiaperNote(request.getDiaperNote());
        record.setSleepDuration(request.getSleepDuration());
        record.setTemperature(request.getTemperature());
        record.setNote(request.getNote());

        return babyRecordRepository.save(record);
    }

    public List<BabyRecord> getRecordsByUser(Long userId) {
        return babyRecordRepository.findByUserIdOrderByRecordTimeDesc(userId);
    }

    private void validate(RecordCreateRequest request) {
        String type = request.getType();
        if (!"feed".equals(type) && !"diaper".equals(type) && !"sleep".equals(type) && !"temp".equals(type) && !"note".equals(type)) {
            throw new InvalidRecordTypeException(type);
        }
    }
}
