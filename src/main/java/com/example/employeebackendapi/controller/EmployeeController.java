package com.example.employeebackendapi.controller;

import com.example.employeebackendapi.dto.*;
import com.example.employeebackendapi.model.Employee;
import com.example.employeebackendapi.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Page<EmployeeResponseDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean active) {
        
        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        return service.getAll(department, active, pageable).map(this::mapToResponseDto);
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto getById(@PathVariable Long id) {
        return mapToResponseDto(service.getById(id));
    }

    @PutMapping("/{id}")
    public EmployeeResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeDetailsRequestDto dto) {
        return mapToResponseDto(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    public EmployeeResponseDto partialUpdate(@PathVariable Long id, @RequestBody UpdateEmployeeDetailsRequestDto dto) {
        return mapToResponseDto(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable Long id) {
        service.softDelete(id);
    }

    @DeleteMapping("/{id}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDelete(@PathVariable Long id) {
        service.hardDelete(id);
    }

    @GetMapping("/salary-range")
    public List<EmployeeResponseDto> getSalaryRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
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
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean active) throws IOException {
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
