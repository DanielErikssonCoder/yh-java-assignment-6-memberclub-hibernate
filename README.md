# Wigell Rental Club - Hibernate Rental System

A rental management system demonstrating ORM concepts and layered architecture, built with Hibernate, MySQL, and JavaFX.

[![Java](https://img.shields.io/badge/Java-25_(Preview)-blue.svg)](https://openjdk.org/)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.6.4-59666C.svg)](https://hibernate.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-orange.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-00758F.svg)](https://www.mysql.com/)
[![JMetro](https://img.shields.io/badge/UI-JMetro-662E9B.svg)](https://pixelduke.com/java-javafx-theme-jmetro/)

## Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Project Structure](#project-structure)
- [Architecture & Design](#architecture--design)
- [How to Run](#how-to-run)
- [Usage](#usage)
- [Technical Implementation](#technical-implementation)

## About the Project

Built as the final assignment in 'Databases and Tests' for the YH education in Java System Development, this project represents a major architectural evolution from file-based storage to a fully relational database system.

The application manages the inventory and rentals for "Wigell Rental System" - a service offering high-end Gaming Computers, Workstations, and Laptops to members. It demonstrates the power of **Object-Relational Mapping (ORM)**, proper layering (Controller-Service-Repository), and advanced JavaFX UI implementation.

## Features

### Core Functionality

- **Database Persistence** - Full CRUD operations backed by MySQL and Hibernate 6.
- **ORM Integration** - Complex entity mapping with annotated classes and sequence generators.
- **Inventory Management** - Specialized handling for different computer types (`GamingComputer`, `Workstation`, `Laptop`) with unique hardware specs.
- **Rental Logic** - Enforces business rules such as member quotas, blocking status, and stock availability.
- **Offline Mode** - Graceful error handling that detects database connection failures at startup.
- **Data Seeding** - Intelligent `DataSeeder` utility that populates the database with test data if empty.

### Modern UI Features

- **JMetro Integration** - Implements a "Native Windows" look and feel (Fluent Design).
- **Custom Window Chrome** - Borderless window with custom resize logic (`WindowResizer`) and title bar controls.
- **Interactive Dashboard** - Animated map visualization using `ScaleTransition` and `FadeTransition`.
- **Responsive Layout** - Dynamic sidebar navigation and panel management.
- **Visual Feedback** - Toast notifications and alert dialogs for user actions.

### Architecture

- **Repository Pattern** - Decouples business logic from data access implementation.
- **Service Layer** - Handles transaction boundaries and validation logic.
- **Singleton Pattern** - Thread-safe management of the Hibernate `SessionFactory`.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com.eriksson.rentalsystemhibernate3/
│   │       ├── Main.java                           # JavaFX Application Entry Point
│   │       ├── Launcher.java                       # Shadow entry for Jar compatibility
│   │       │
│   │       ├── controller/                         # UI Controllers (MVC)
│   │       │   ├── MainController.java             # Root layout & window management
│   │       │   ├── DashboardController.java        # KPI metrics & statistics
│   │       │   ├── MapController.java              # Interactive map animations
│   │       │   ├── RentalController.java           # Rental transactions & returns
│   │       │   ├── ComputerController.java         # Inventory list & management
│   │       │   ├── AddComputerController.java      # Add/Edit computer dialog
│   │       │   ├── MemberController.java           # Member CRUD operations
│   │       │   ├── SidebarController.java          # Navigation menu logic
│   │       │   ├── RightPanelController.java       # Contextual side panel
│   │       │   └── SettingsController.java         # App configuration
│   │       │
│   │       ├── entity/                             # JPA Entities (Domain Model)
│   │       │   ├── Member.java                     # Club member with quota tracking
│   │       │   ├── Rental.java                     # Transaction record
│   │       │   ├── RentalType.java                 # Enum: GAMING, WORKSTATION, LAPTOP
│   │       │   ├── GamingComputer.java             # GPU & processor specs
│   │       │   ├── Workstation.java                # RAID & multi-monitor setup
│   │       │   └── Laptop.java                     # Portable computer entity
│   │       │
│   │       ├── service/                            # Business Logic Layer
│   │       │   ├── MemberService.java              # Member validation & blocking rules
│   │       │   ├── RentalService.java              # Checkout, return, quota enforcement
│   │       │   ├── GamingComputerService.java      # Gaming PC specific logic
│   │       │   ├── WorkstationService.java         # Workstation management
│   │       │   └── LaptopService.java              # Laptop operations
│   │       │
│   │       ├── repo/                               # Data Access Layer
│   │       │   ├── MemberRepository.java           # Interface
│   │       │   ├── MemberRepositoryImpl.java       # Hibernate implementation
│   │       │   ├── RentalRepository.java
│   │       │   ├── RentalRepositoryImpl.java
│   │       │   ├── GamingComputerRepository.java
│   │       │   ├── GamingComputerRepositoryImpl.java
│   │       │   ├── WorkstationRepository.java
│   │       │   ├── WorkstationRepositoryImpl.java
│   │       │   ├── LaptopRepository.java
│   │       │   └── LaptopRepositoryImpl.java
│   │       │
│   │       ├── exception/                          # Custom Domain Exceptions
│   │       │   ├── EntityNotFoundException.java
│   │       │   ├── QuotaExceededException.java
│   │       │   ├── MemberBlockedException.java
│   │       │   ├── MemberAlreadyExistsException.java
│   │       │   ├── MemberHasActiveRentalsException.java
│   │       │   ├── ItemAlreadyRentedException.java
│   │       │   ├── RentalAlreadyReturnedException.java
│   │       │   ├── InvalidMemberDataException.java
│   │       │   └── InvalidRentalDataException.java
│   │       │
│   │       ├── ui/                                 # UI Utilities
│   │       │   └── App.java                        # Theme & style management
│   │       │
│   │       └── util/                               # Core Utilities
│   │           ├── HibernateUtil.java              # SessionFactory Singleton
│   │           ├── DataSeeder.java                 # Test data population
│   │           ├── WindowResizer.java              # Custom window resize handler
│   │           └── AlertHelper.java                # Dialog & notification utilities
│   │
│   └── resources/
│       ├── com.eriksson.rentalsystemhibernate3/
│       │   ├── main-view.fxml                      # Main application layout
│       │   ├── sidebar.fxml                        # Navigation sidebar
│       │   ├── dashboard-view.fxml                 # Dashboard with map
│       │   ├── computer-view.fxml                  # Inventory table
│       │   ├── add-computer-view.fxml              # Add/Edit dialog
│       │   ├── member-view.fxml                    # Member management
│       │   ├── rental-view.fxml                    # Rental transactions
│       │   └── settings-view.fxml                  # Settings panel
│       │
│       ├── images/
│       │   └── app-icon.png                        # Application icon
│       │
│       ├── hibernate.properties                    # Database configuration
│       └── style.css                               # Custom CSS overrides
│
└── test/
    ├── java/
    │   └── com.eriksson.rentalhibernate3/
    │       ├── repo/
    │       │   └── RentalRepositoryIntegrationTest.java
    │       └── service/
    │           └── RentalServiceTest.java
    │
    └── resources/
        ├── hibernate.properties                    # Test DB configuration
        └── mockito-extensions/
            └── org.mockito.plugins.MockMaker       # Mockito configuration
```

## Architecture & Design

### Multi-Layered Architecture

**1. Presentation Layer (View/Controller)**
- Uses FXML for layout definition.
- Controllers utilize `JMetro` for styling.
- Strictly handles UI logic; delegates business operations to Services.

**2. Service Layer**
- The "Brain" of the application.
- Orchestrates transactions.
- Validates data (e.g., ensuring a member isn't blocked before renting).
- Converts Exceptions into user-friendly messages.

**3. Repository Layer**
- Implements the **DAO/Repository Pattern**.
- Abstracts Hibernate `Session` and `Transaction` management.
- Performs HQL (Hibernate Query Language) and Native SQL queries.

**4. Persistence Layer**
- MySQL Database (`wigell_rental_db`).
- Entities mapped via JPA Annotations (`@Entity`, `@Table`, `@Id`).

### Design Patterns Implemented

**Singleton Pattern**
- `HibernateUtil` ensures a single, globally accessible `SessionFactory` to manage connection pooling efficiently.

**Factory/Strategy (Implicit in Hibernate)**
- Hibernate handles the creation of concrete SQL based on the Dialect and Entity configurations.

**Observer Pattern**
- JavaFX `ObservableList` and properties used to keep the UI in sync with the data model.

## How to Run

### Prerequisites

- **Java 25** (Must support Preview Features)
- **MySQL Server 8.0+**
- **Maven 3.8+**

### Database Setup

1. Ensure your MySQL server is running on `localhost:3306`.
2. Creates the schema automatically (no manual SQL needed), but you need to configure the connection.
3. Open `src/main/resources/hibernate.properties` and update your credentials:

```properties
hibernate.connection.url=jdbc:mysql://localhost:3306/wigell_rental_db?createDatabaseIfNotExist=true
hibernate.connection.username=YOUR_USERNAME
hibernate.connection.password=YOUR_PASSWORD
hibernate.hbm2ddl.auto=update
```

### Build & Run

```bash
# Clone the repository
git clone https://github.com/danielerikssoncoder/rental-system-hibernate-3.git

# Navigate to project directory
cd rental-system-hibernate-3

# Run with Maven (Windows)
.\mvnw.cmd clean javafx:run

# Or on Unix/Linux/Mac
./mvnw clean javafx:run
```

*Note: Since the project uses Java 25 preview features, the Maven compiler configuration includes `<release>25</release>`.*

## Usage

### Typical Workflow

1. **Dashboard** - View the animated availability map and quick stats.
2. **Inventory** - Add new hardware. Select type (Gaming/Workstation/Laptop) to see dynamic fields (e.g., GPU vs RAID Config).
3. **Members** - Register new members. The system checks for duplicate emails.
4. **Rentals** - Go to the Rental tab. Select a member and a computer. The system validates quotas.
5. **Returns** - Process returns via the active rentals list.

### Database Persistence

- **Auto-Schema Generation:** Hibernate's `hbm2ddl.auto=update` ensures tables are created/updated on startup.
- **Connection Check:** `HibernateUtil.checkConnection()` verifies DB status before loading the UI.

## Technical Implementation

### Hibernate ORM

- **Configuration:** Programmatic setup via `HibernateUtil` reading from properties.
- **Inheritance:** Concrete classes (`GamingComputer`, `Workstation`, `Laptop`) mapped to specific tables for optimized querying.
- **Generators:** Uses `@SequenceGenerator` for robust ID creation.

### JavaFX & JMetro

- **Styling:** Overrides the default "Modena" theme with JMetro Dark/Light theme.
- **Animations:** Custom implementation in `MapController` using standard JavaFX Transitions.
- **Window Management:** `WindowResizer` class allows resizing of the undecorated stage (borderless window).

### Testing

- **Unit Testing:** JUnit 5 for Service logic.
- **Mocking:** Mockito used to mock Repository responses when testing Services.
- **Integration Testing:** Tests designed to run against H2 in-memory DB or local MySQL.

---

**Author:** Daniel Eriksson  
**Course:** Java System Development (YH)  
**Assignment:** Hibernate & Database Architecture   
**Date:** February 2026   
