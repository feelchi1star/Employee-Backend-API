package com.example.employeebackendapi.dto;
import lombok.Data;
import java.time.LocalDateTime;

public record EmployeeResponseDto {
    private Long id;
    private String name;
    private String email;
    private String department;
    private Double salary;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}