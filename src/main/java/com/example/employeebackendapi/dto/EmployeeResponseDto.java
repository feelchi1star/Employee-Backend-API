package com.example.employeebackendapi.dto;

import java.time.LocalDateTime;

public record EmployeeResponseDto(
    Long id,
    String name,
    String email,
    String department,
    Double salary,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}