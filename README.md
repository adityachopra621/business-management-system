# Integrated Business Management System

A professional, enterprise-grade **Integrated Business Management System** built with **Java 21**, **JavaFX**, and **PostgreSQL**. Designed for small-to-medium businesses to manage Inventory, Purchasing, Sales & Invoicing, Customers, Suppliers, Expenses, and Comprehensive Financial Reporting.

---

## 1. System Architecture & Features

### Core Modules
- **Executive Dashboard:** Real-time KPI summary cards (Total Revenue, Total Expenses, Net Profit, Inventory Asset Value, Low Stock Alerts, Product Counts), interactive monthly BarCharts and top-selling product PieCharts, and live recent activity logs.
- **Inventory Management:** Full Product CRUD, category filtering, instant keyword search, min-stock warning thresholds, low-stock filter view, and Excel export.
- **Supplier & Purchase Management:** Supplier profiles with full contact details, multi-item purchase order generation, automated inventory increment upon receiving, and payment status tracking (`PENDING`/`PAID`).
- **Sales Management & Invoicing:** Multi-item sales transactions, discount application, automatic stock availability verification (prevents sales when stock is insufficient), automated inventory deduction, and professional PDF invoice generation via Apache PDFBox.
- **Expense Tracking:** Expense classification across categories (Rent, Salaries, Utilities, Logistics, Marketing, etc.), daily and monthly expense summaries, and Excel export.
- **Financial Reports & Analytics:**
  - Revenue report by date range
  - Expense analysis by category
  - Full Profit & Loss (P&L) statement with gross profit and margin percentages
  - Product-level profitability analysis (Unit cost vs. selling price vs. profit margin)
  - One-click export of any report to **PDF** (Apache PDFBox) and **Excel** (Apache POI).

---

## 2. Java Syllabus Concepts Demonstrated

| Concept | Implementation in Project | File Reference |
| :--- | :--- | :--- |
| **Object-Oriented Programming (OOP)** | Inheritance (`GenericDao<T>`, custom exceptions extending `BusinessException`), Encapsulation (all models with private fields & BigDecimal), Interfaces (`GenericDao`, `Initializable`), Polymorphism | `model/*`, `dao/GenericDao.java`, `exception/*` |
| **Collections Framework** | `ArrayList` (order items, record lists), `LinkedList` / `List`, `HashMap` / `LinkedHashMap` (chronological chart data & top products), `HashSet` (distinct category and metric filtering), Iterators | `dao/DashboardDao.java`, `service/ReportService.java`, `controller/DashboardController.java` |
| **Generics & Comparators** | `GenericDao<T>` interface for database operations, `Comparable<Category>` for natural sorting, JavaFX generic table columns | `dao/GenericDao.java`, `model/Category.java` |
| **Exception Handling** | Robust custom hierarchy: `BusinessException`, `DatabaseException`, `InsufficientStockException`, `ValidationException`, checked SQL handling with automatic transaction rollback | `exception/*`, `dao/SaleDao.java`, `dao/PurchaseDao.java` |
| **File Handling & Serialization** | Apache PDFBox document generation, Apache POI `.xlsx` generation, `FileHelper` with `ObjectOutputStream`/`ObjectInputStream` for model serialization/deserialization | `util/PdfExporter.java`, `util/ExcelExporter.java`, `util/FileHelper.java` |
| **Logging & Properties** | `java.util.logging.Logger` through `LoggerFactory`, externalized configuration loaded from `application.properties` and `log.properties` | `util/LoggerFactory.java`, `util/AppConfig.java`, `resources/*.properties` |
| **JDBC & SQL Operations** | `PreparedStatement`, `ResultSet`, parameterized queries, connection pooling, automated schema initialization on first launch | `util/DatabaseConnection.java`, `dao/*` |
| **Database Transactions** | Explicit ACID transactions (`conn.setAutoCommit(false)`, `commit()`, `rollback()`) with row locking (`FOR UPDATE`) in sales and purchases to prevent race conditions and negative inventory | `dao/SaleDao.java`, `dao/PurchaseDao.java` |
| **JavaFX GUI** | Modern desktop UI with Stage, Scene, FXML, CSS stylesheet, TableView, custom CellFactories, Charts (BarChart, PieChart), Spinners, DatePickers, Dialogs, and responsive layouts | `src/main/resources/fxml/*`, `src/main/resources/styles/styles.css`, `controller/*` |

---

## 3. Technology Stack

- **Language:** Java 21 (LTS)
- **GUI Framework:** JavaFX 22 (`javafx-controls`, `javafx-fxml`, `javafx-graphics`)
- **Database:** PostgreSQL (JDBC Driver 42.7.4)
- **Build & Dependency Tool:** Apache Maven
- **PDF Generation:** Apache PDFBox 3.0.3
- **Excel Generation:** Apache POI 5.3.0 & `poi-ooxml`
- **Logging:** `java.util.logging`

---

## 4. Database Setup & Initialization

### 1. Install & Start PostgreSQL
Ensure PostgreSQL is running locally on port `5432`.

### 2. Create the Database
Open `psql` or pgAdmin and run:
```sql
CREATE DATABASE business_mgmt;
```

### 3. Configure Database Credentials
Edit `src/main/resources/application.properties` if your PostgreSQL username or password differs from the defaults:
```properties
db.url=jdbc:postgresql://localhost:5432/business_mgmt
db.user=postgres
db.password=postgres
app.name=Business Management System
app.version=1.0.0
```

### 4. Automatic Schema Execution
When the application starts, `DatabaseConnection.java` automatically runs `src/main/resources/db/schema.sql` to create all tables, foreign keys, and indexes if they do not already exist.

### 5. Load Sample Seed Data
To populate sample categories, products, suppliers, customers, expenses, and initial stock:
```bash
psql -U postgres -d business_mgmt -f src/main/resources/db/sample_data.sql
```

---

## 5. Build and Execution Instructions

### Prerequisites
- JDK 21+ installed and configured on your `PATH`
- Maven 3.8+ installed and configured on your `PATH`

### Step 1: Compile the Project
```bash
mvn clean compile
```

### Step 2: Run the JavaFX Application
```bash
mvn javafx:run
```

### Step 3: Package as Standalone Application (Uber-JAR)
```bash
mvn clean package
java -jar target/business-management-system-1.0.0.jar
```

---

## 6. Directory Structure

```
business-management-system/
├── pom.xml                                   # Maven dependencies and build plugins
├── README.md                                 # Full documentation and guide
└── src/main/
    ├── java/com/businessmgmt/
    │   ├── App.java                          # JavaFX main entry point
    │   ├── controller/                       # UI Controller Layer (MVC)
    │   │   ├── MainController.java           # Sidebar navigation & view switcher
    │   │   ├── DashboardController.java      # Dashboard metrics & charts
    │   │   ├── InventoryController.java      # Product catalog & stock
    │   │   ├── ProductFormController.java    # Add/edit product dialog
    │   │   ├── SupplierController.java       # Supplier directory
    │   │   ├── SupplierFormController.java   # Add/edit supplier dialog
    │   │   ├── PurchaseController.java       # Purchase orders & payment status
    │   │   ├── PurchaseFormController.java   # Create purchase order with line items
    │   │   ├── SalesController.java          # Sales transactions & PDF invoices
    │   │   ├── SaleFormController.java       # Create sale, line items & discounts
    │   │   ├── ExpenseController.java        # Expense management
    │   │   ├── ExpenseFormController.java    # Add/edit expense dialog
    │   │   ├── CustomerController.java       # Customer records
    │   │   ├── CustomerFormController.java   # Add/edit customer dialog
    │   │   └── ReportsController.java        # Financial reports & PDF/Excel exports
    │   ├── dao/                              # Data Access Object Layer
    │   │   ├── GenericDao.java               # Generic DAO interface
    │   │   ├── ProductDao.java               # Product SQL CRUD & low-stock queries
    │   │   ├── CategoryDao.java              # Category SQL CRUD
    │   │   ├── SupplierDao.java              # Supplier SQL CRUD
    │   │   ├── CustomerDao.java              # Customer SQL CRUD
    │   │   ├── PurchaseDao.java              # ACID Purchase transactions (+ inventory)
    │   │   ├── SaleDao.java                  # ACID Sale transactions (- inventory)
    │   │   ├── ExpenseDao.java               # Expense SQL CRUD & category aggregation
    │   │   ├── StockMovementDao.java         # Stock audit trail records
    │   │   └── DashboardDao.java             # Aggregations, monthly metrics & charts
    │   ├── exception/                        # Custom Exception Hierarchy
    │   │   ├── BusinessException.java        # Base business logic exception
    │   │   ├── DatabaseException.java        # SQL and connection wrapper
    │   │   ├── InsufficientStockException.java # Prevents negative inventory
    │   │   └── ValidationException.java      # Input validation error
    │   ├── model/                            # Data Models / Entities
    │   │   ├── Product.java                  # Product entity (Serializable)
    │   │   ├── Category.java                 # Category entity (Comparable)
    │   │   ├── Supplier.java                 # Supplier entity
    │   │   ├── Customer.java                 # Customer entity
    │   │   ├── Purchase.java                 # Purchase header
    │   │   ├── PurchaseItem.java             # Purchase line item
    │   │   ├── Sale.java                     # Sale header & invoice
    │   │   ├── SaleItem.java                 # Sale line item
    │   │   ├── Expense.java                  # Expense record
    │   │   ├── StockMovement.java            # Stock movement log
    │   │   └── DashboardStats.java           # Aggregated statistics container
    │   ├── service/                          # Business Logic Service Layer
    │   │   ├── InventoryService.java
    │   │   ├── SupplierService.java
    │   │   ├── CustomerService.java
    │   │   ├── PurchaseService.java
    │   │   ├── SalesService.java
    │   │   ├── ExpenseService.java
    │   │   ├── DashboardService.java
    │   │   └── ReportService.java
    │   └── util/                             # Utilities & Helpers
    │       ├── AlertHelper.java              # JavaFX dialog helpers
    │       ├── AppConfig.java                # Properties config loader
    │       ├── DatabaseConnection.java       # JDBC connection manager & schema runner
    │       ├── DateHelper.java               # Date formatting utilities
    │       ├── ExcelExporter.java            # Apache POI Excel export
    │       ├── FileHelper.java               # Object serialization & file IO
    │       ├── LoggerFactory.java            # java.util.logging wrapper
    │       ├── PdfExporter.java              # Apache PDFBox PDF export
    │       └── ValidationHelper.java         # Validation rules
    └── resources/
        ├── application.properties            # DB and application properties
        ├── log.properties                    # Logging configuration
        ├── db/
        │   ├── schema.sql                    # PostgreSQL normalized schema
        │   └── sample_data.sql               # Initial sample seed dataset
        ├── fxml/                             # FXML Screen Layouts
        │   ├── MainView.fxml
        │   ├── DashboardView.fxml
        │   ├── InventoryView.fxml
        │   ├── ProductFormDialog.fxml
        │   ├── SupplierView.fxml
        │   ├── SupplierFormDialog.fxml
        │   ├── PurchaseView.fxml
        │   ├── PurchaseFormDialog.fxml
        │   ├── SalesView.fxml
        │   ├── SaleFormDialog.fxml
        │   ├── ExpenseView.fxml
        │   ├── ExpenseFormDialog.fxml
        │   ├── CustomerView.fxml
        │   ├── CustomerFormDialog.fxml
        │   └── ReportsView.fxml
        └── styles/
            └── styles.css                    # Modern CSS theme & component styling
```
