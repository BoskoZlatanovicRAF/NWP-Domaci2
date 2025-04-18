# Custom framework - Spring Wannabe

## Overview
This project is a lightweight Java-based HTTP framework inspired by Spring Boot. It provides a foundation for building web applications with dependency injection and MVC architecture patterns. The framework handles HTTP requests, routes them to appropriate controllers, and manages object dependencies automatically.

## Key Features

### Dependency Injection
- Annotation-based dependency injection system (`@Autowired`, `@Service`, `@Component`, etc.)
- Support for interface injection with qualifiers
- Singleton and prototype scope management
- Automatic dependency resolution

### MVC Architecture
- Controller-based request handling with `@Controller` annotation
- Request mapping with `@GET`, `@POST`, and `@Path` annotations
- Support for path parameters in URL routing
- JSON response serialization

### Component Discovery
- Automatic scanning and discovery of annotated classes
- Support for beans, services, components, and controllers
- Qualifier-based implementation selection
- Runtime dependency initialization

### HTTP Server
- Built-in HTTP server implementation
- Request parsing and routing
- Response generation with proper HTTP headers
- Support for GET and POST methods

## Example Application
The project includes a sample student management API with:
- Student model with basic properties
- In-memory repository implementation
- Service layer for business logic
- REST controller with CRUD operations

## Technologies Used

### Core
- Java 11
- Maven for dependency management and building

### Libraries
- Google Gson for JSON processing

### Design Patterns
- Dependency Injection pattern
- MVC (Model-View-Controller) pattern
- Repository pattern
- Service pattern
- Singleton pattern

### Framework Components
- Annotation-based configuration
- Reflection API for runtime introspection
- Custom exception handling
- Multithreaded request processing

## Project Structure
- `framework`: Core framework components
  - `annotations`: Custom annotations for configuration
  - `di`: Dependency injection engine
  - `discovery`: Component scanning mechanism
  - `request`: HTTP request handling
  - `response`: HTTP response generation
  - `route`: URL routing system
- `example`: Sample application
  - `controller`: REST controllers
  - `model`: Domain models
  - `repository`: Data access layer
  - `service`: Business logic layer
- `server`: HTTP server implementation
