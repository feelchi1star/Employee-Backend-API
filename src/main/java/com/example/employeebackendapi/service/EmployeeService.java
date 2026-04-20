package com.example.employeebackendapi.service;

import java.util.List;

import com.example.employeebackendapi.dto.EmployeeRequestDto;
import com.example.employeebackendapi.dto.UpdateEmployeeDetailsRequestDto;
import com.example.employeebackendapi.model.Employee;

import jakarta.validation.Valid;

public interface EmployeeService {
    // Get all employees
    List<Employee> getAll();
    // Get employee by ID
    Employee getById(Long id);
    // Create new employee
    Employee create(@Valid EmployeeRequestDto employee);

    // Update existing employee
    Employee update(Long id, @Valid UpdateEmployeeDetailsRequestDto employee);

    // Delete employee by ID
    void softDelete(Long id);

    // Hard delete employee by ID
    void hardDelete(Long id);
}
