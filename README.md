# Employee Backend API

A high-performance, robust RESTful API built with Spring Boot for comprehensive employee management. This project features advanced reporting capabilities, bulk data processing, and a clean, developer-friendly interface.

## 🚀 Features

- **Comprehensive CRUD**: Full management of employee records with validation.
- **Advanced Reporting**:
  - 📊 **Excel Export**: Generate detailed spreadsheets with custom styling.
  - 📄 **PDF Generation**: Professional PDF reports with headers, footers, and dynamic formatting.
- **Bulk Processing**: High-speed employee import from Excel files with row-by-row validation.
- **Search & Filtering**: dynamic filtering by department, active status, and salary ranges.
- **Clean API Design**:
  - Simplified pagination metadata.
  - Explicit parameter naming for cross-environment stability.
  - Global exception handling with structured, meaningful error messages.
- **Documentation**: Integrated Swagger/OpenAPI UI for easy endpoint testing.

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.4.x
- **Database**: H2 (In-memory for development)
- **Persistence**: Spring Data JPA / Hibernate
- **Reporting**:
  - Apache POI (Excel)
  - LibrePDF/OpenPDF (PDF)
- **Documentation**: SpringDoc OpenAPI
- **Utilities**: Lombok, Jakarta Validation

## 📂 Resources & Testing

- **Postman Collection**: Located at `src/main/resources/postman/Employee_API_Collection.postman_json`. Import this into Postman to quickly test all endpoints.
- **Sample Excel File**: A template for bulk imports can be found at `src/main/resources/sample/employee_import_sample.xlsx` (or check the resources folder). Use this structure for successful imports.

## 🏁 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Application

1.  Clone the repository.
2.  Navigate to the project root.
3.  Run the application using Maven:
    ```bash
    ./mvnw spring-boot:run
    ```
4.  Access the API at `http://localhost:8080/api/v1/employees`.
5.  View Swagger documentation at `http://localhost:8080/api/v1/swagger-ui.html`.

---

## 👤 Author

**feechi1star**

- **Role**: Lead Backend Developer
- **Environment**: Linux / IntelliJ IDEA
- **Focus**: Java, Spring Boot, API Architecture

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
