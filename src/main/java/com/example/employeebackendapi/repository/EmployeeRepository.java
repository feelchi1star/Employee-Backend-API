package com.example.employeebackendapi.repository;
import java.lang.StackWalker.Option;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.employeebackendapi.model.Employee;

/**
 * Repository for Employee database operations.
 * Author: Feechi1star
 * Date: 2026-04-17
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Custom query method to find employees by department
    List<Employee> findByDepartment(String department);

    // Custom query method to find an employee by email
    Optional<Employee> findByEmail(String email);
    

    // list of employee with activeTrue
    List<Employee> findByActiveTrue();

    // list of employee by salary range
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :min AND :max")
    List<Employee> findBySalaryRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

}