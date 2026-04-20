package com.example.employeebackendapi.service.Impl;

import com.example.employeebackendapi.dto.EmployeeRequestDto;
import com.example.employeebackendapi.dto.ImportEmployeeRequestDto;
import com.example.employeebackendapi.dto.UpdateEmployeeDetailsRequestDto;
import com.example.employeebackendapi.dto.ImportResultDto;
import com.example.employeebackendapi.exception.EmployeeNotFoundException;
import com.example.employeebackendapi.exception.InvalidFileFormatException;
import com.example.employeebackendapi.exception.DuplicateEmailException;
import com.example.employeebackendapi.model.Employee;
import com.example.employeebackendapi.repository.EmployeeRepository;
import com.example.employeebackendapi.service.EmployeeService;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Element;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Validated
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final Validator validator;

    public EmployeeServiceImpl(EmployeeRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public Page<Employee> getAll(String department, Boolean active, Pageable pageable) {
        Specification<Employee> spec = (root, query, cb) -> cb.conjunction();

        if (department != null && !department.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department"), department));
        }
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        
        return repository.findAll(spec, pageable);
    }

    @Override
    public Employee getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
    }

    @Override
    @Transactional
    public Employee create(EmployeeRequestDto dto) {
        // 1) Check for duplicate email
        duplicateEmailCheck(dto.email());

        // 2) Validate salary based on department
        validateSalary(dto.salary(), dto.department());

        Employee employee = new Employee();
        employee.setFirstName(dto.firstName());
        employee.setLastName(dto.lastName());
        employee.setEmail(dto.email());
        employee.setDepartment(dto.department());
        employee.setSalary(dto.salary());
        employee.setDateOfJoining(LocalDate.now());

        return repository.save(employee);
    }

    @Override
    @Transactional
    public Employee update(Long id, UpdateEmployeeDetailsRequestDto dto) {
        // Check if employee exists
        Employee employee = this.repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        // Validation for salary
        validateSalary(dto.salary(), dto.department());

        employee.setSalary(dto.salary());
        employee.setDepartment(dto.department());

        return repository.save(employee);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        // Check if employee exists
        Employee employee = this.repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        
        employee.setActive(false);
        repository.save(employee);
    }

    @Override
    @Transactional
    public void hardDelete(Long id) {
        // Check if employee exists
        Employee employee = this.repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        
        if (employee.getActive()) {
            throw new IllegalArgumentException("Cannot hard delete an active employee");
        }
        repository.delete(employee);
    }

    @Override
    public List<Employee> getBySalaryRange(BigDecimal min, BigDecimal max) {
        return repository.findBySalaryRange(min, max);
    }

    @Override
    @Transactional
    public ImportResultDto importFromExcel(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new InvalidFileFormatException("Only .xlsx files are supported");
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    ImportEmployeeRequestDto dto = mapRowToDto(row, formatter);
                    Set<ConstraintViolation<ImportEmployeeRequestDto>> violations = validator.validate(dto);
                    
                    if (!violations.isEmpty()) {
                        String errMsg = violations.stream()
                                .map(v -> v.getPropertyPath() + " - " + v.getMessage())
                                .collect(Collectors.joining(", "));
                        errors.add("Row " + i + ": " + errMsg);
                        failureCount++;
                        continue;
                    }

                    // Also check validations from original create process during import
                    duplicateEmailCheck(dto.email());
                    validateSalary(dto.salary(), dto.department());

                    Employee employee = new Employee();
                    employee.setFirstName(dto.firstName());
                    employee.setLastName(dto.lastName());
                    employee.setEmail(dto.email());
                    employee.setDepartment(dto.department());
                    employee.setSalary(dto.salary());
                    employee.setDateOfJoining(dto.dateOfJoining());
                    employee.setActive(dto.isActive());
                    
                    repository.save(employee);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Row " + i + ": " + e.getMessage());
                    failureCount++;
                }
            }
        }
        return new ImportResultDto(successCount, failureCount, errors);
    }

    @Override
    public void exportToExcel(HttpServletResponse response, String department, Boolean active) throws IOException {
        List<Employee> employees = getAll(department, active, Pageable.unpaged()).getContent();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");
            
            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "First Name", "Last Name", "Email", "Department", "Salary", "Joining Date", "Status", "Created At", "Updated At"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle altStyle = workbook.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
            DateTimeFormatter dtmf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            int rowIdx = 1;
            for (Employee e : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getFirstName());
                row.createCell(2).setCellValue(e.getLastName());
                row.createCell(3).setCellValue(e.getEmail());
                row.createCell(4).setCellValue(e.getDepartment());
                row.createCell(5).setCellValue(e.getSalary().doubleValue());
                row.createCell(6).setCellValue(e.getDateOfJoining().format(dtf));
                row.createCell(7).setCellValue(e.getActive() ? "Active" : "Inactive");
                row.createCell(8).setCellValue(e.getCreatedAt() != null ? e.getCreatedAt().format(dtmf) : "");
                row.createCell(9).setCellValue(e.getUpdatedAt() != null ? e.getUpdatedAt().format(dtmf) : "");

                if (rowIdx % 2 == 0) {
                    for (int i = 0; i < columns.length; i++) {
                        row.getCell(i).setCellStyle(altStyle);
                    }
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=employees_" + System.currentTimeMillis() + ".xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public void exportToPdf(HttpServletResponse response) throws IOException {
        List<Employee> employees = repository.findAll();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        
        HeaderFooter footer = new HeaderFooter(new Phrase("Page "), new Phrase(" of total"));
        document.setFooter(footer);
        
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Employee Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        document.add(new Paragraph("Company: Employee API"));
        document.add(new Paragraph("Date: " + LocalDateTime.now()));
        document.add(new Paragraph("Total Records: " + employees.size()));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        String[] headers = {"ID", "Full Name", "Email", "Department", "Salary", "Joining Date", "Status"};
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
        
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;

        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            addTableCell(table, e.getId().toString(), i);
            addTableCell(table, e.getFirstName() + " " + e.getLastName(), i);
            addTableCell(table, e.getEmail(), i);
            addTableCell(table, e.getDepartment(), i);
            
            PdfPCell salaryCell = new PdfPCell(new Phrase(currencyFormat.format(e.getSalary()), getEmployeeFont(e)));
            salaryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            if (i % 2 != 0) salaryCell.setBackgroundColor(new Color(230, 240, 255));
            table.addCell(salaryCell);

            addTableCell(table, e.getDateOfJoining().format(dtf), i);
            addTableCell(table, e.getActive() ? "Active" : "Inactive", i);
        }

        document.add(table);
        document.close();
    }

    private void addTableCell(PdfPTable table, String text, int index) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        if (index % 2 != 0) {
            cell.setBackgroundColor(new Color(230, 240, 255));
        }
        table.addCell(cell);
    }
    
    private Font getEmployeeFont(Employee e) {
        if (!e.getActive()) {
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.STRIKETHRU);
            font.setColor(Color.GRAY);
            return font;
        }
        return FontFactory.getFont(FontFactory.HELVETICA, 10);
    }

    private ImportEmployeeRequestDto mapRowToDto(Row row, DataFormatter formatter) {
        String firstName = formatter.formatCellValue(row.getCell(0));
        String lastName = formatter.formatCellValue(row.getCell(1));
        String email = formatter.formatCellValue(row.getCell(2));
        String department = formatter.formatCellValue(row.getCell(3));
        
        BigDecimal salary = BigDecimal.ZERO;
        String salaryStr = formatter.formatCellValue(row.getCell(4));
        if (salaryStr != null && !salaryStr.isBlank()) {
            salary = new BigDecimal(salaryStr.replaceAll("[^0-9.]", ""));
        }

        LocalDate dateOfJoining = LocalDate.now();
        String dateStr = formatter.formatCellValue(row.getCell(5));
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                dateOfJoining = LocalDate.parse(dateStr);
            } catch (Exception e) {
                // If parsing fails, it will be caught by validator in importFromExcel
            }
        }

        Boolean active = true;
        String activeStr = formatter.formatCellValue(row.getCell(6));
        if (activeStr != null && !activeStr.isBlank()) {
            active = Boolean.parseBoolean(activeStr);
        }

        return new ImportEmployeeRequestDto(firstName, lastName, email, department, salary, dateOfJoining, active);
    }

    private void duplicateEmailCheck(String email) {
        this.repository.findByEmail(email).ifPresent(e -> {
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
