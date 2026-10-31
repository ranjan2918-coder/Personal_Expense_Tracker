# Personal Expense Tracker — Java + MariaDB + REST API

A modular **Java 8** desktop application with a **Swing GUI**, a built-in **REST API** (tested with Postman), and a **MariaDB** database backend powered by **XAMPP**.

---

## ✨ Features

| Feature | Description |
|---|---|
| **Swing GUI** | Dark-themed desktop UI with Dashboard, Add, View, Search, Report, Budget screens |
| **REST API** | Embedded `HttpServer` on port `8080` — no Spring Boot required |
| **MariaDB DB** | Full JDBC persistence via XAMPP; data survives app restarts |
| **Postman Ready** | Import the included collection and test all endpoints instantly |
| **Modular Design** | Clean separation across `model`, `dao`, `service`, `api`, `gui` packages |

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 8 |
| GUI | Java Swing |
| REST API | `com.sun.net.httpserver.HttpServer` (JDK built-in) |
| Database | MariaDB via XAMPP (port 3306) |
| DB Driver | MariaDB Connector/J (`mariadb-java-client-3.x.x.jar`) |
| DB Tool | phpMyAdmin |
| API Testing | Postman |
| IDE | Eclipse |

---

## 📁 Project Structure

```
PersonalExpenseTracker/
├── src/expense/
│   ├── api/
│   │   ├── ApiServer.java          ← Starts REST API on port 8080
│   │   ├── ExpenseHandler.java     ← GET/POST/PUT/DELETE /api/expenses
│   │   └── BudgetHandler.java      ← GET/POST/PUT/DELETE /api/budgets
│   ├── dao/
│   │   ├── ExpenseDAO.java         ← JDBC CRUD for expenses table
│   │   └── BudgetDAO.java          ← JDBC CRUD for budgets table
│   ├── database/
│   │   ├── DatabaseConfig.java     ← DB connection settings (edit here)
│   │   └── DatabaseConnection.java ← Singleton JDBC connection
│   ├── exception/
│   │   └── InvalidExpenseException.java
│   ├── gui/
│   │   ├── ExpenseTrackerGUI.java  ← Main entry point (launches GUI + API)
│   │   ├── DashboardPanel.java
│   │   ├── AddExpensePanel.java
│   │   ├── ViewExpensesPanel.java
│   │   ├── SearchExpensePanel.java
│   │   ├── MonthlyReportPanel.java
│   │   ├── BudgetPanel.java
│   │   ├── AppTheme.java
│   │   └── ComponentFactory.java
│   ├── model/
│   │   ├── Expense.java
│   │   ├── Budget.java
│   │   └── Reportable.java
│   ├── service/
│   │   └── ExpenseService.java     ← Business logic layer
│   ├── util/
│   │   ├── JsonUtil.java           ← Zero-dependency JSON helper
│   │   └── ExpenseFileManager.java ← Legacy (kept for reference)
│   └── app/
│       └── PersonalExpenseTracker.java ← Legacy CLI (unchanged)
├── lib/
│   └── mariadb-java-client-3.x.x.jar  ← Download separately (see Step 1)
├── database_setup.sql              ← Run this in phpMyAdmin
├── ExpenseTracker_Postman_Collection.json  ← Import into Postman
├── .classpath
└── .gitignore
```

---

## 🗄️ Database Schema

**Database name:** `expense_tracker`

### `expenses` table
| Column | Type | Notes |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Primary key |
| `title` | VARCHAR(200) | Required |
| `amount` | DECIMAL(12,2) | Required, must be > 0 |
| `category` | VARCHAR(100) | Required |
| `description` | TEXT | Optional |
| `expense_date` | DATE | Required, cannot be future |
| `created_at` | TIMESTAMP | Auto-set |

### `budgets` table
| Column | Type | Notes |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Primary key |
| `category` | VARCHAR(100) | Required |
| `monthly_limit` | DECIMAL(12,2) | Required, must be > 0 |
| `month` | TINYINT | 1–12 |
| `year` | SMALLINT | e.g. 2026 |
| `created_at` | TIMESTAMP | Auto-set |

---

## 🚀 Quick Setup Guide

### Step 1 — Get the MariaDB JDBC Driver

> ⚠️ The `lib/` folder needs this JAR — not included in the repo.

1. Go to: https://mariadb.com/downloads/connectors/
2. Download **Connector/J** for **Java 8+**
3. Copy the `.jar` into `lib/` folder

---

### Step 2 — Start XAMPP

1. Open **XAMPP Control Panel**
2. Click **Start** next to **Apache** and **MySQL**
3. Both should turn green

---

### Step 3 — Create the Database

1. Open **http://localhost/phpmyadmin**
2. Click the **SQL** tab
3. Paste the contents of `database_setup.sql` and click **Go**

---

### Step 4 — Add the JAR to Eclipse

1. Right-click project → **Properties** → **Java Build Path** → **Libraries**
2. Click **Add External JARs...** → select the JAR from `lib/`
3. Click **Apply and Close**, then press **F5** to refresh

---

### Step 5 — Run the Application

1. In Eclipse, open `src/expense/gui/ExpenseTrackerGUI.java`
2. Right-click → **Run As → Java Application**
3. The **GUI window** opens and the **REST API starts** on port 8080

**Console output:**
```
============================================
  Expense Tracker REST API started
  URL: http://localhost:8080
  Health check: http://localhost:8080/api/health
============================================
[DB] Connected to expense_tracker on localhost:3306
Personal Expense Tracker GUI started.
```

---

## 🌐 REST API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | API health check |
| `GET` | `/api/expenses` | List all expenses |
| `GET` | `/api/expenses/{id}` | Get expense by ID |
| `POST` | `/api/expenses` | Add new expense |
| `PUT` | `/api/expenses/{id}` | Update expense |
| `DELETE` | `/api/expenses/{id}` | Delete expense |
| `GET` | `/api/budgets` | List all budgets |
| `POST` | `/api/budgets` | Add new budget |

### Sample: POST /api/expenses
**Request body:**
```json
{
  "title": "Lunch at Restaurant",
  "amount": 350.00,
  "category": "Food",
  "description": "Team lunch outing",
  "expense_date": "2026-10-31"
}
```
**Response (201 Created):**
```json
{
  "message": "Expense added successfully",
  "id": 6
}
```

### Sample: POST /api/budgets
**Request body:**
```json
{
  "category": "Travel",
  "monthly_limit": 5000.00,
  "month": 11,
  "year": 2026
}
```

---

## 📮 Postman Testing

1. Open **Postman**
2. Click **Import**
3. Drag and drop `ExpenseTracker_Postman_Collection.json` from the project root
4. The collection **"Personal Expense Tracker API"** loads with all 8 requests pre-configured

---

## ⚙️ Configuration

To change database credentials, edit only `DatabaseConfig.java`:

```java
public static final String HOST     = "localhost";
public static final int    PORT     = 3306;
public static final String DB_NAME  = "expense_tracker";
public static final String USERNAME = "root";
public static final String PASSWORD = "";   // XAMPP default
```

---

## 🖥️ GUI Screens

| Screen | Features |
|---|---|
| **Dashboard** | Total expenses, this-month total, record count, budget status, category bar chart |
| **Add Expense** | Form with category dropdown, custom category support, date picker |
| **View Expenses** | Sortable table, inline Update dialog, Delete with confirmation |
| **Search** | Filter expenses by category, shows category total |
| **Monthly Report** | Select any month, see all expenses + category breakdown |
| **Budget** | Set monthly budget, progress bar, over-budget warning |

---

## 🔧 Troubleshooting

| Error | Fix |
|---|---|
| `ClassNotFoundException: org.mariadb.jdbc.Driver` | JAR missing from `lib/`. Re-do Steps 1 & 4. |
| `Communications link failure` | XAMPP MySQL not running. Start it in XAMPP. |
| `Unknown database 'expense_tracker'` | Run `database_setup.sql` in phpMyAdmin. |
| `Port 8080 already in use` | Change `PORT` in `ApiServer.java` to e.g. `8081`. |
| Postman: `Connection refused` | Run `ExpenseTrackerGUI` in Eclipse first. |

---

## 📋 Requirements

- Java 8 or later (JDK)
- Eclipse IDE
- XAMPP (Apache + MySQL/MariaDB)
- MariaDB Connector/J JAR (downloaded separately)
- Postman (for API testing)
