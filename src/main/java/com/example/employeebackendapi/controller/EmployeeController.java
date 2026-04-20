package com.example.employeebackendapi.controller;

import com.example.employeebackendapi.dto.*;
import com.example.employeebackendapi.model.Employee;
import com.example.employeebackendapi.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponseDto create(@Valid @RequestBody EmployeeRequestDto dto) {
        return mapToResponseDto(service.create(dto));
    }

    @GetMapping
    public PagedResponseDto<EmployeeResponseDto> getAll(
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "active", required = false) Boolean active,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        
        Page<EmployeeResponseDto> page = service.getAll(department, active, pageable).map(this::mapToResponseDto);
        
        return new PagedResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto getById(@PathVariable("id") Long id) {
        return mapToResponseDto(service.getById(id));
    }

    @PutMapping("/{id}")
    public EmployeeResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody UpdateEmployeeDetailsRequestDto dto) {
        return mapToResponseDto(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public EmployeeResponseDto partialUpdate(@PathVariable("id") Long id, @RequestBody UpdateEmployeeDetailsRequestDto dto) {
        return mapToResponseDto(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable("id") Long id) {
        service.softDelete(id);
    }

    @DeleteMapping("/{id}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDelete(@PathVariable("id") Long id) {
        service.hardDelete(id);
    }

    @GetMapping("/salary-range")
    public List<EmployeeResponseDto> getSalaryRange(
            @RequestParam("min") BigDecimal min,
            @RequestParam("max") BigDecimal max) {
        return service.getBySalaryRange(min, max).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @PostMapping("/import")
    public ImportResultDto importEmployees(@RequestParam("file") MultipartFile file) throws IOException {
        return service.importFromExcel(file);
    }

    @GetMapping("/export/excel")
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "active", required = false) Boolean active) throws IOException {
        service.exportToExcel(response, department, active);
    }

    @GetMapping("/export/pdf")
    public void exportToPdf(HttpServletResponse response) throws IOException {
        service.exportToPdf(response);
    }

    private EmployeeResponseDto mapToResponseDto(Employee employee) {
        return new EmployeeResponseDto(
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getSalary().doubleValue(),
                employee.getActive(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
