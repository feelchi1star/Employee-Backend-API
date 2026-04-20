package com.example.employeebackendapi.service;

import com.example.employeebackendapi.dto.*;
import com.example.employeebackendapi.model.Employee;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface EmployeeService {
    Page<Employee> getAll(String department, Boolean active, Pageable pageable);
    
    Employee getById(Long id);
    
    Employee create(EmployeeRequestDto dto);

    Employee update(Long id, UpdateEmployeeDetailsRequestDto dto);

    void softDelete(Long id);

    void hardDelete(Long id);

    List<Employee> getBySalaryRange(BigDecimal min, BigDecimal max);

    ImportResultDto importFromExcel(MultipartFile file) throws IOException;

    void exportToExcel(HttpServletResponse response, String department, Boolean active) throws IOException;

    void exportToPdf(HttpServletResponse response) throws IOException;
}
