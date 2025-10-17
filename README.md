# Flower Shop Management System

[![SonarQube](https://img.shields.io/badge/SonarQube-checked-blue?logo=sonarqube)](https://st-lab-ci.inf.tu-dresden.de/sonarqube/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.x-59666C?logo=hibernate&logoColor=white)](https://hibernate.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)

[![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-blueviolet)](https://en.wikipedia.org/wiki/Modular_programming)
[![DDD](https://img.shields.io/badge/Pattern-Domain%20Driven%20Design-important)](https://en.wikipedia.org/wiki/Domain-driven_design)
[![Code Style](https://img.shields.io/badge/Code%20Style-Clean%20Code-success)](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)

Enterprise-grade web application for comprehensive flower shop operations management. Built on Spring Boot and Salespoint framework, providing end-to-end digitalization of retail workflows, service contracts, and financial operations.

## Architecture

**Architectural Style:** Modular Monolith with Domain-Driven Design

The application follows a **layered modular architecture** organized by business domains. Each module encapsulates its own domain logic, persistence, and presentation layers while sharing common infrastructure through Spring's dependency injection.

**Key Architectural Decisions:**
- **Vertical Slicing**: Modules organized by business capability, not technical layer
- **Spring MVC Pattern**: Controllers → Services → Repositories
- **Domain Model**: Rich entities with business logic (DDD approach)
- **Repository Pattern**: Data access abstraction via Spring Data JPA
- **Service Layer**: Transaction boundaries and business orchestration

**Technology Stack:**
- Java 21
- Spring Boot 3.x + Spring Security
- Salespoint Framework (domain framework for retail/POS systems)
- Thymeleaf templating
- Hibernate ORM
- H2/PostgreSQL database
- Apache PDFBox for document generation

**Domain Modules:**
```
flowershop/
├── calendar/          # Event scheduling & time management
├── clock/             # Time tracking system
├── finances/          # Financial operations & reporting
├── inventory/         # Stock management & wholesaler orders
├── product/           # Product catalog & pricing
├── sales/             # POS transactions & billing
└── services/          # Contract management & plant service
```

## Features

### Core Capabilities
- **Inventory Management**: Real-time stock tracking with automated reorder points
- **Sales Processing**: Point-of-sale system with instant pricing calculation
- **Service Contracts**: Long-term plant maintenance agreements with recurring billing
- **Event Management**: Wedding/event order scheduling with automated procurement
- **Financial Reporting**: Daily cash flow reconciliation and P&L statements
- **Wholesaler Integration**: Direct ordering system with confirmation workflow

### Business Logic
- Automatic event-based ordering (T-1 day procurement trigger)
- Multi-tier pricing (wholesale, retail, service contracts)
- Revenue recognition for services
- Loss tracking for perishable inventory
- PDF invoice generation

## Prerequisites

**Runtime Requirements:**
- JDK 21+
- Maven 3.8+
- 1GB RAM minimum

**Supported Browsers:**
- Firefox 130+
- Chrome/Chromium 130+
- Opera 114+
- Safari 18+

## Quick Start

**Development Mode:**
```bash
./mvnw spring-boot:run
```

**Production Build:**
```bash
./mvnw clean package
java -jar target/swt24w07-1.0.0.BUILD-SNAPSHOT.jar
```

**Run Tests:**
```bash
./mvnw test
```

Application starts at: `http://localhost:8080`

## Project Structure

```
src/
├── main/
│   ├── asciidoc/              # Technical documentation
│   │   ├── developer_documentation.adoc
│   │   ├── pflichtenheft.adoc
│   │   └── time_recording.adoc
│   ├── java/flowershop/       # Application source
│   └── resources/
│       ├── application.properties
│       ├── templates/         # Thymeleaf views
│       └── static/            # CSS, JS, assets
└── test/java/flowershop/      # Unit & integration tests
```

## Configuration

Key properties (`application.properties`):
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
server.port=8080
```

## Security

- Spring Security with role-based access control
- CSRF protection enabled
- H2 console access restricted
- Frame options configured for iframe embedding

## Development

**Code Quality:**
- SonarQube integration for static analysis
- JUnit 5 for testing
- Maven enforcer for dependency management

**Hot Reload:**
```bash
# Spring Boot DevTools enabled
# Changes auto-reload on classpath update
```

## Documentation

- [Developer Documentation](src/main/asciidoc/developer_documentation.adoc) - System architecture & design
- [Requirements Specification](src/main/asciidoc/pflichtenheft.adoc) - Functional requirements
- [Meeting Protocols](src/main/asciidoc/protocols/) - Development decisions
- [Time Recording](src/main/asciidoc/time_recording.adoc) - Project tracking

## Dependencies

**Core Frameworks:**
- `spring-boot-starter-web` - REST & MVC
- `spring-boot-starter-thymeleaf` - Templating
- `spring-boot-starter-security` - Authentication
- `salespointframework` - Business domain framework

**Additional Libraries:**
- `pdfbox:3.0.3` - PDF generation
- `easytable:1.0.2` - Table rendering

Full dependency tree: `./mvnw dependency:tree`

## Build & Deploy

**CI/CD:**
- GitHub Actions for automated builds
- SonarQube quality gates
- Maven release plugin for versioning

**Deployment:**
- Executable JAR deployment
- Spring Boot production-ready actuators
- Environment-specific profiles supported

## License

Apache License 2.0

## Academic Context

Software Engineering Lab Project  
Winter Semester 2024

---

**Maintainer:** mrEkso (Davyd Okaianchenko)