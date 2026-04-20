package com.example.employeebackendapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequestDto(
    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Department is required")
    String department,

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be positive")
    BigDecimal salary,

    @NotNull(message = "Date of joining is required")
    @PastOrPresent(message = "Date of joining cannot be in the future")
    LocalDate dateOfJoining,

    Boolean active
) {
    public EmployeeRequestDto {
        if (active == null) active = true;
    }
}