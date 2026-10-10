package com.example.babycare.api.controller;

import com.example.babycare.api.dto.request.LoginRequest;
import com.example.babycare.api.dto.request.RecordCreateRequest;
import com.example.babycare.api.dto.response.ApiResponse;
import com.example.babycare.api.dto.response.UserResponse;
import com.example.babycare.application.service.AuthService;
import com.example.babycare.application.service.RecordService;
import com.example.babycare.domain.model.BabyRecord;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@RequestBody @Valid LoginRequest request) {
        UserResponse response = authService.login(request.getCode(), request.getNickName(), request.getAvatarUrl());
        return ApiResponse.success(response);
    }
}

@RestController
@RequestMapping("/api")
class RecordController {

    private final RecordService recordService;

    RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/record")
    public ApiResponse<BabyRecord> createRecord(@RequestBody @Valid RecordCreateRequest request) {
        return ApiResponse.success(recordService.createRecord(request));
    }

    @GetMapping("/records/{userId}")
    public ApiResponse<List<BabyRecord>> getRecords(@PathVariable Long userId) {
        return ApiResponse.success(recordService.getRecordsByUser(userId));
    }
}
