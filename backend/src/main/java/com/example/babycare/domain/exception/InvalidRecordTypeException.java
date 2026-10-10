package com.example.babycare.domain.exception;

public class InvalidRecordTypeException extends BusinessException {
    public InvalidRecordTypeException(String type) {
        super("不支持的记录类型: " + type);
    }
}
