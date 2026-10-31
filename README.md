# 💰 Personal Expense Tracker

A **Java Swing** desktop application to track personal expenses, set monthly budgets, and generate reports — backed by a **MySQL** database via **JDBC**.

> Built for college Java coursework. No Spring Boot or Hibernate — pure Java 8 + JDBC + Swing.

---

## 📸 Screenshots

| Dashboard | Add Expense | View Expenses |
|-----------|-------------|---------------|
| Real-time totals, category breakdown, budget status | Form with title, category, amount, date, description | Sortable table with Update & Delete |

| Search | Monthly Report | Budget |
|--------|----------------|--------|
| Search by category or keyword | Per-month breakdown with category totals | Visual progress bar with budget alerts |

---

## 🚀 Features

- 🔐 **User Login & Registration** — per-user data isolation
- ➕ **Add Expenses** — title, category, amount, date, description
- 📋 **View All Expenses** — sortable table (by date / amount)
- ✏️ **Update & Delete** — inline edit dialog
- 🔍 **Search** — by category or keyword (searches title + description)
- 📊 **Monthly Report** — per-month expense list + category breakdown
- 💼 **Budget Management** — set monthly limit, visual progress bar, overspend alert
- 📈 **Dashboard** — total spent, this month, record count, budget status, category bars

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 8 |
| GUI | Java Swing |
| Database | MySQL 8.x |
| Connectivity | JDBC (Java Database Connectivity) |
| IDE | Eclipse |
| Driver | MySQL Connector/J |

---

## 📁 Project Structure

```
PersonalExpenseTracker/
├── src/
│   └── expense/
│       ├── app/
│       │   └── PersonalExpenseTracker.java   # Console entry point
│       ├── dao/
│       │   ├── ExpenseDAO.java               # CRUD for expenses table
│       │   ├── BudgetDAO.java                # CRUD for budgets table
│       │   └── UserDAO.java                  # Login / register
│       ├── database/
│       │   └── DBConnection.java             # JDBC singleton connection
│       ├── exception/
│       │   └── InvalidExpenseException.java
│       ├── gui/
│       │   ├── ExpenseTrackerGUI.java         # Main window (run this!)
│       │   ├── LoginPanel.java               # Login / Register screen
│       │   ├── DashboardPanel.java
│       │   ├── AddExpensePanel.java
│       │   ├── ViewExpensesPanel.java
│       │   ├── SearchExpensePanel.java
│       │   ├── MonthlyReportPanel.java
│       │   ├── BudgetPanel.java
│       │   ├── AppTheme.java                 # Colors, fonts, borders
│       │   └── ComponentFactory.java         # Reusable styled widgets
│       ├── model/
│       │   ├── Expense.java
│       │   ├── Budget.java
│       │   ├── User.java
│       │   └── Reportable.java               # Interface
│       ├── service/
│       │   └── ExpenseService.java           # Business logic layer
│       └── util/
│           └── ExpenseFileManager.java       # Legacy (kept for reference)
├── lib/
│   └── mysql-connector-j-*.jar              # ← Place JAR here
├── SQL_SETUP.sql                             # Database setup script
└── .classpath
```

---

## ⚙️ Setup Instructions

### 1. Install MySQL

Download **MySQL Community Server 8.x** from:  
👉 https://dev.mysql.com/downloads/mysql/

Note your **root password** during installation.

---

### 2. Create the Database

Open **MySQL Workbench** → connect to your local server → run the script:

```
File → Open SQL Script → select SQL_SETUP.sql → ⚡ Execute
```

This creates the `personal_expense_tracker` database with three tables:
- `users` — login accounts
- `expenses` — all expense records
- `budgets` — monthly budget limits

**Verify:**
```sql
USE personal_expense_tracker;
SHOW TABLES;
-- Should show: budgets, expenses, users
```

---

### 3. Configure Database Credentials

Open `src/expense/database/DBConnection.java` and edit:

```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/personal_expense_tracker?...";
private static final String DB_USER     = "root";        // ← your MySQL username
private static final String DB_PASSWORD = "root";        // ← your MySQL password
```

---

### 4. Add MySQL Connector/J JAR

Download from 👉 https://dev.mysql.com/downloads/connector/j/  
*(Select "Platform Independent" ZIP → extract it)*

Copy the JAR into the `lib/` folder:
```
PersonalExpenseTracker/lib/mysql-connector-j-9.2.0.jar
```

In Eclipse:
```
Right-click project → Build Path → Add External JARs
→ navigate to lib/ → select the JAR → Open
```

Verify the JAR appears under **Referenced Libraries** in Package Explorer.

> If your JAR version differs from `9.2.0`, edit `.classpath` to match:
> ```xml
> <classpathentry kind="lib" path="lib/mysql-connector-j-YOUR_VERSION.jar"/>
> ```

---

### 5. Run the Application

```
Right-click ExpenseTrackerGUI.java → Run As → Java Application
```

The app will:
1. Test the DB connection (shows error popup if it fails)
2. Show the **Login / Register** screen
3. After login → open the full **Dashboard**

---

## 🗄️ Database Schema

```sql
-- Users table
CREATE TABLE users (
    user_id  INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Expenses table
CREATE TABLE expenses (
    expense_id   INT            PRIMARY KEY AUTO_INCREMENT,
    user_id      INT            NOT NULL,
    title        VARCHAR(100)   NOT NULL,
    amount       DECIMAL(12,2)  NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    description  TEXT,
    expense_date DATE           NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Budgets table
CREATE TABLE budgets (
    budget_id     INT            PRIMARY KEY AUTO_INCREMENT,
    user_id       INT            NOT NULL,
    category      VARCHAR(50)    DEFAULT 'Overall',
    monthly_limit DECIMAL(12,2)  NOT NULL,
    month         INT            NOT NULL,
    year          INT            NOT NULL,
    UNIQUE KEY (user_id, category, month, year),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

---

## 🔗 Architecture

```
GUI Panels  (expense.gui)
     ↓
ExpenseService  (business logic + validation)
     ↓
DAO Layer  (ExpenseDAO · BudgetDAO · UserDAO)
     ↓
DBConnection  (JDBC singleton)
     ↓
MySQL — personal_expense_tracker
```

---

## 🐛 Troubleshooting

| Error | Fix |
|-------|-----|
| "Cannot connect to MySQL" | Start MySQL service: Task Manager → Services → MySQL80 → Start |
| "JDBC Driver not found" | Verify JAR is in `lib/` and added to Eclipse Build Path |
| "Access denied for user" | Wrong password in `DBConnection.java` |
| "Unknown database" | Run `SQL_SETUP.sql` in MySQL Workbench first |
| Red underlines in Eclipse | Right-click project → Build Path → Configure Build Path → verify JAR is listed |

---

## 📝 Categories Supported

`Food` · `Travel` · `Bills` · `Shopping` · `Entertainment` · `Health` · `Education` · `Rent` · `Other (custom)`

---

## 👨‍💻 Author

**Ranjan S**  
Java | MySQL | JDBC | Swing  
College Java Project — 2026
