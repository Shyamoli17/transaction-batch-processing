# transaction-batch-processing

This project is a Spring Boot application that leverages Spring Batch for robust batch processing. It is configured to use PostgreSQL as the database and demonstrates how to process and persist data efficiently using Spring Batch, JPA, and JDBC.

## Features

- Batch processing with Spring Batch
- Database integration with PostgreSQL
- JPA and JDBC support for data access
- Configurable batch jobs and steps
- Example of processing and transforming input data

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database

## Getting Started

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd spring-batch-processing
```

### 2. Configure the Database

Ensure you have a PostgreSQL instance running and update the connection details in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb?currentSchema=transaction_processing
    username: 
    password: 
    driver-class-name: org.postgresql.Driver
```

> Make sure the database and schema (`transaction_processing`) exist.

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will initialize the batch schema and execute configured batch jobs.

## Project Structure

- `src/main/java/com/example/rest/springbatch/`  
  Main application source code, including batch configuration and processing logic.
- `src/main/resources/application.yml`  
  Application configuration, including database and batch settings.

## Configuration

- Batch schema is initialized automatically (`initialize-schema: always`).
- Batch jobs can be configured and reloaded via the `batch.cache.memory.reload-jobs` property.

## References

- [Spring Batch Documentation](https://docs.spring.io/spring-boot/3.4.4/how-to/batch.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.4/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Spring Data JDBC](https://docs.spring.io/spring-boot/3.4.4/reference/data/sql.html#data.sql.jdbc)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Creating a Batch Service](https://spring.io/guides/gs/batch-processing/)

---

For any questions or contributions, please open an issue or submit a pull request.
