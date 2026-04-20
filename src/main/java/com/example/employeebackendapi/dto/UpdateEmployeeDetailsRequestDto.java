package com.example.employeebackendapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateEmployeeDetailsRequestDto(
    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    BigDecimal salary,

    @NotBlank(message = "Department is required")
    String department,

    boolean active
) {}