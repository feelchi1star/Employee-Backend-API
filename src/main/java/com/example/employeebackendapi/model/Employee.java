package com.example.employeebackendapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Employee entity representing the employee table in the database.
 */
@Entity
@Table(name = "employees" )
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
   
    @NotBlank
    @Size(max = 50)
    private String firstName;
   
    @NotBlank
    @Size(max = 50)
    private String lastName;
   
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
   
    @NotBlank
    @Size(max = 100)
    private String department;
   
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal salary;
   
    @NotNull
    @PastOrPresent
    private LocalDate dateOfJoining;
   
    @NotNull
    private Boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;   

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
