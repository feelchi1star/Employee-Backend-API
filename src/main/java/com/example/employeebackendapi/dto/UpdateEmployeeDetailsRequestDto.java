package com.example.employeebackendapi.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
/**
 * DTO for creating/updating employee.
 */
@Data
public class UpdateEmployeeDetailsRequestDto {


    // Salary should be a positive number
    @NotNull
    @Positive
    private BigDecimal salary;


    @NotBlank
    private String department;

    private boolean active;
}