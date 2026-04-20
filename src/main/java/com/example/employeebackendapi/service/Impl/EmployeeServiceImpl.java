package com.example.employeebackendapi.service.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.employeebackendapi.dto.EmployeeRequestDto;
import com.example.employeebackendapi.dto.UpdateEmployeeDetailsRequestDto;
import com.example.employeebackendapi.exception.DuplicateEmailException;
import com.example.employeebackendapi.exception.EmployeeNotFoundException;
import com.example.employeebackendapi.model.Employee;
import com.example.employeebackendapi.repository.EmployeeRepository;
import com.example.employeebackendapi.service.EmployeeService;

import jakarta.validation.Valid;

@Validated
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }


    @Override
    public List<Employee> getAll() {
        return this.employeeRepository.findAll();
    }

    @Override
    public Employee getById(Long id) {
        return this.employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
    }



    @Override
    public Employee create(EmployeeRequestDto employee) {
        // 1) Check for duplicate email
        duplicateEmailCheck(employee.getEmail());

        // 2) Validate salary based on department
        validateSalary(employee.getSalary(), employee.getDepartment());

        Employee newEmployee = new Employee();
        newEmployee.setFirstName(employee.getFirstName());
        newEmployee.setLastName(employee.getLastName());
        newEmployee.setEmail(employee.getEmail());
        newEmployee.setDepartment(employee.getDepartment());
        newEmployee.setSalary(employee.getSalary());
        newEmployee.setDateOfJoining(LocalDate.now());
        newEmployee.setActive(true);

        return this.employeeRepository.save(newEmployee);
    }

    @Override
    public Employee update(Long id, UpdateEmployeeDetailsRequestDto employee) {


    validateSalary(employee.getSalary(), employee.getDepartment());

      Employee employDetail=  this.employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

      employDetail.setSalary(employee.getSalary());
      employDetail.setDepartment(employee.getDepartment());
      employDetail.setActive(employee.isActive());

        return this.employeeRepository.save(employDetail);
    }

    @Override
    public void softDelete(Long id) {
        Employee e = this.employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        e.setActive(false);
        this.employeeRepository.save(e);
    }

    @Override
    public void hardDelete(Long id) {
        Employee e = this.employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        this.employeeRepository.delete(e);
    }
    
    private void duplicateEmailCheck(String email) {
        this.employeeRepository.findByEmail(email).ifPresent(e -> {
            throw new DuplicateEmailException("Email already exists: " + email);
        });
    }

    private void validateSalary(BigDecimal amount, String department) {
        if ("Intern".equalsIgnoreCase(department)) {
            if (amount.floatValue() < 15000) throw new IllegalArgumentException("Min 15000 for Interns");
        } else {
            if (amount.floatValue() < 30000) throw new IllegalArgumentException("Min 30000 for other departments");
        }
    }
}
