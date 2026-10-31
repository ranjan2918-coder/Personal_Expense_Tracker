-- =============================================================
--  SQL_SETUP.sql  –  Personal Expense Tracker Database Setup
-- =============================================================
--
-- STEP-BY-STEP INSTRUCTIONS
-- ==========================
--
-- 1. INSTALL MySQL (if not already installed)
--    Download MySQL Community Server from:
--      https://dev.mysql.com/downloads/mysql/
--    During setup, note the root password you set – you will need it.
--    Recommended version: MySQL 8.x
--
-- 2. OPEN MySQL Command Line Client or MySQL Workbench
--    - Command Line: Start -> MySQL 8.0 Command Line Client
--    - Workbench:    Start -> MySQL Workbench -> open a query tab
--
-- 3. RUN THIS SCRIPT
--    In MySQL Workbench:
--      File -> Open SQL Script -> select this file -> click the lightning bolt (Execute)
--    In Command Line:
--      source C:/path/to/SQL_SETUP.sql;
--
-- 4. DOWNLOAD MySQL Connector/J (JDBC Driver)
--    https://dev.mysql.com/downloads/connector/j/
--      - Select "Platform Independent" zip
--      - Extract it – you will find:  mysql-connector-j-X.X.X.jar
--      - Rename it to:                mysql-connector-j-9.2.0.jar  (or edit .classpath)
--
-- 5. ADD THE JAR TO YOUR ECLIPSE PROJECT
--    a. Create a folder named  lib  inside your Eclipse project root:
--         PersonalExpenseTracker/lib/
--    b. Copy  mysql-connector-j-9.2.0.jar  into that folder.
--    c. In Eclipse: right-click the project -> Build Path -> Add External JARs
--       -> navigate to the lib folder -> select the jar -> Open.
--    d. Verify: the jar should now appear under "Referenced Libraries" in Package Explorer.
--
-- 6. CONFIGURE DATABASE CREDENTIALS
--    Open:  src/expense/database/DBConnection.java
--    Edit these three constants at the top of the file:
--      DB_URL      = "jdbc:mysql://localhost:3306/personal_expense_tracker?..."
--      DB_USER     = "root"           <- your MySQL username
--      DB_PASSWORD = "root"           <- your MySQL password
--
-- 7. RUN THE APPLICATION
--    In Eclipse: right-click  ExpenseTrackerGUI.java  -> Run As -> Java Application
--
-- 8. TEST THE DATABASE CONNECTION
--    If the DB is unreachable, a popup error will appear on launch.
--    Check: MySQL service is running (Services -> MySQL80 -> Start)
--
-- =============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS personal_expense_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE personal_expense_tracker;

-- ─────────────────────────────────────────────────────────────
--  Table: users
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id   INT          NOT NULL AUTO_INCREMENT,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────
--  Table: expenses
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS expenses (
    expense_id   INT            NOT NULL AUTO_INCREMENT,
    user_id      INT            NOT NULL,
    title        VARCHAR(100)   NOT NULL,
    amount       DECIMAL(12, 2) NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    description  TEXT,
    expense_date DATE           NOT NULL,
    PRIMARY KEY (expense_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_expense_user    (user_id),
    INDEX idx_expense_date    (expense_date),
    INDEX idx_expense_category(category)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────
--  Table: budgets
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS budgets (
    budget_id     INT            NOT NULL AUTO_INCREMENT,
    user_id       INT            NOT NULL,
    category      VARCHAR(50)    NOT NULL DEFAULT 'Overall',
    monthly_limit DECIMAL(12, 2) NOT NULL,
    month         INT            NOT NULL,  -- 1-12
    year          INT            NOT NULL,
    PRIMARY KEY (budget_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_budget (user_id, category, month, year)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────────────────────
--  Sample data (optional – delete if you want a fresh start)
-- ─────────────────────────────────────────────────────────────
-- INSERT INTO users (username, password) VALUES ('demo', 'demo123');

-- To verify the tables were created:
-- SHOW TABLES;
-- DESCRIBE users;
-- DESCRIBE expenses;
-- DESCRIBE budgets;

-- =============================================================
--  Verification queries (run these in Workbench to confirm)
-- =============================================================
-- SELECT * FROM users;
-- SELECT * FROM expenses;
-- SELECT * FROM budgets;
