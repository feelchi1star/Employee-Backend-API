package com.example.employeebackendapi.dto;

import java.util.List;

/**
 * Clean wrapper for paginated responses.
 * Removes redundant Spring Data Page metadata.
 */
public record PagedResponseDto<T>(
    List<T> content,
    int pageNo,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {
}
