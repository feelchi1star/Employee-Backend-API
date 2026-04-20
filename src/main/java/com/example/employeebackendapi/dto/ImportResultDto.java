package com.example.employeebackendapi.dto;

import java.util.List;

/**
 * DTO representing Excel import result.
 */

public record ImportResultDto(
        int successCount,
        int failureCount,
        List<String> errors) {
}