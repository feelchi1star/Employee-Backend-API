package com.example.employeebackendapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.employeebackendapi.dto.EmployeeRequestDto;
import com.example.employeebackendapi.dto.EmployeeResponseDto;
import com.example.employeebackendapi.dto.UpdateEmployeeDetailsRequestDto;
import com.example.employeebackendapi.model.Employee;
import com.example.employeebackendapi.service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

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
    @ResponseStatus(code = HttpStatus.CREATED)
    public EmployeeResponseDto create(@Valid @RequestBody EmployeeRequestDto dto) {
        Employee employee = service.create(dto);
        return mapToResponseDto(employee);
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<EmployeeResponseDto> getAll() {
        return service.getAll().stream()
                .map(this::mapToResponseDto)
                .toList();
    }



    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public EmployeeResponseDto getById(@PathVariable Long id) {
        Employee employee = service.getById(id);
        return mapToResponseDto(employee);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public EmployeeResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeDetailsRequestDto dto) {
        Employee updated = service.update(id, dto);
        return mapToResponseDto(updated);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public EmployeeResponseDto partialUpdate(@PathVariable Long id, @RequestBody UpdateEmployeeDetailsRequestDto dto) {
        Employee updated = service.update(id, dto);
        return mapToResponseDto(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable Long id) {
        service.softDelete(id);
    }

    @DeleteMapping("/{id}/hard")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void hardDelete(@PathVariable Long id) {
        service.hardDelete(id);
    }

    @GetMapping("/salary-range")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EmployeeResponseDto> getSalaryRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return service.getAll().stream()
                .filter(e -> e.getSalary().compareTo(min) >= 0 && e.getSalary().compareTo(max) <= 0)
                .map(this::mapToResponseDto)
                .toList();
    }

    private EmployeeResponseDto mapToResponseDto(Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setName(employee.getFirstName() + " " + employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());
        dto.setSalary(employee.getSalary().doubleValue());
        dto.setActive(employee.getActive());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        return dto;
    }
}
