# Bookkeeping Application

A Spring Boot-based bookkeeping application that provides a robust backend for managing financial records and transactions.

## Features

- RESTful API for managing financial records
- PostgreSQL database integration
- Database migrations using Flyway
- OpenAPI documentation
- Docker support for easy deployment
- Test containers for integration testing

## Prerequisites

- Java 17
- Maven
- Docker and Docker Compose
- PostgreSQL (optional, as it's included in Docker setup)

## Getting Started

### Running with Docker Compose

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd bookkeeping
   ```

2. Start the application and database using Docker Compose:
   ```bash
   docker-compose --file compose-run.yaml up 
   ```

The application will be available at `http://localhost:8080`

### Running Locally

1. Ensure you have Java 17 and Maven installed
2. Start the PostgreSQL database using Docker:
   ```bash
   docker-compose up database
   ```
3. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation

Once the application is running, you can access the OpenAPI documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Database Configuration

The application uses PostgreSQL with the following default configuration:
- Port: 15432
- Database: bookkeeping
- Username: user
- Password: user

## Development

### Building the Project

```bash
./mvnw clean package
```

### Running Tests

```bash
./mvnw test
```

## Project Structure

- `src/main/java`: Main application code
- `src/main/resources`: Configuration files and resources
- `src/test`: Test classes and resources
- `src/main/resources/db/migration`: Database migration scripts
