# NASA APOD Service

A reactive RESTful service built with Quarkus that integrates with NASA's Astronomy Picture of the Day (APOD) API. This service fetches, stores, and manages astronomy pictures with their associated metadata.

## 🚀 Features

- Fetch and store NASA APOD data
- Pagination support for retrieving stored APOD entries
- Reactive endpoints using Mutiny
- Swagger UI documentation
- Comprehensive error handling
- Unit test coverage

## 🛠 Tech Stack

- Java 17
- Quarkus
- Hibernate Reactive with Panache
- PostgreSQL
- SmallRye OpenAPI (Swagger)
- RESTEasy Reactive
- JUnit 5 & Mockito

## 📋 Prerequisites

- Java 17 or higher
- Docker & Docker Compose
- Maven
- NASA API Key ([Get it here](https://api.nasa.gov/))

## ⚙️ Configuration

Create an `application.properties` file in `src/main/resources` with the following configurations:

```properties
# NASA API Configuration
nasa.api.key=your-api-key-here

# Database Configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=your-username
quarkus.datasource.password=your-password
quarkus.datasource.reactive.url=postgresql://localhost:5432/your-database

# Swagger UI
quarkus.swagger-ui.path=/q/swagger-ui
quarkus.swagger-ui.always-include=true
```

## 🚀 Running the Application

1. Start the PostgreSQL database:
```bash
docker-compose up -d
```

2. Run the application in dev mode:
```bash
./mvnw quarkus:dev
```

3. Access the application:
- Main application: http://localhost:9090
- Swagger UI: http://localhost:9090/q/swagger-ui/

## 📚 API Documentation

The API documentation is available through Swagger UI at `http://localhost:9090/q/swagger-ui/`

### Available Endpoints

#### Fetch and Save APOD Data
```http
POST /api/v1/astronomy/fetch
```
Fetches APOD data from NASA API for a specified date range and saves it to the database.

Query Parameters:
- `startDate`: Start date in YYYY-MM-DD format
- `endDate`: End date in YYYY-MM-DD format

#### Get All APOD Data
```http
GET /api/v1/astronomy
```
Retrieves stored APOD data with pagination support.

Query Parameters:
- `page`: Page number (default: 0)
- `size`: Items per page (default: 10)
- `sortBy`: Field to sort by (default: "date")
- `sortDirection`: Sort direction ("asc" or "desc", default: "desc")

## 🧪 Running Tests

Run the test suite with:
```bash
./mvnw test
```

## 📝 Code Quality

This project follows these practices:
- Comprehensive unit testing
- Code documentation
- Error handling
- Input validation
- Reactive programming patterns

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -m 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔍 Swagger UI Screenshots

### How to Access Swagger UI
1. Start the application
2. Navigate to http://localhost:9090/q/swagger-ui/
3. Explore available endpoints and their documentation

### Key Documentation Features in Swagger UI
- Interactive API documentation
- Request/Response examples
- Try-it-out functionality
- Model schemas
- Authentication requirements

## ⚠️ Known Issues

- The application currently doesn't support concurrent API calls to NASA's APOD service
- Rate limiting is not implemented for the NASA API calls

## 🔮 Future Improvements

1. Add caching layer for frequently accessed data
2. Implement rate limiting for NASA API calls
3. Add support for batch operations
4. Enhance error reporting and monitoring
5. Add API versioning support