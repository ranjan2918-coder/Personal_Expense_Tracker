-- ============================================================
--  Personal Expense Tracker – Database Setup Script
--  Database : expense_tracker
--  Server   : XAMPP MariaDB (localhost:3306)
--
--  HOW TO USE:
--    1. Start Apache and MySQL (MariaDB) in XAMPP Control Panel
--    2. Open phpMyAdmin → http://localhost/phpmyadmin
--    3. Click the "SQL" tab at the top
--    4. Paste this entire script and click "Go"
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS expense_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Select the database
USE expense_tracker;

-- ============================================================
--  Table: expenses
-- ============================================================
CREATE TABLE IF NOT EXISTS expenses (
    id           INT          NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200) NOT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    category     VARCHAR(100) NOT NULL,
    description  TEXT         DEFAULT NULL,
    expense_date DATE         NOT NULL,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  Table: budgets
-- ============================================================
CREATE TABLE IF NOT EXISTS budgets (
    id            INT           NOT NULL AUTO_INCREMENT,
    category      VARCHAR(100)  NOT NULL,
    monthly_limit DECIMAL(12,2) NOT NULL,
    month         TINYINT       NOT NULL COMMENT '1-12',
    year          SMALLINT      NOT NULL,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_budget (category, month, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  Sample data (optional – delete if you prefer a clean start)
-- ============================================================
INSERT INTO expenses (title, amount, category, description, expense_date) VALUES
    ('Grocery Shopping',  1500.00, 'Food',          'Monthly groceries',     CURDATE()),
    ('Electricity Bill',  2200.00, 'Bills',          'October electricity',   CURDATE()),
    ('Metro Card Recharge', 500.00,'Travel',         'Monthly metro pass',    CURDATE()),
    ('Netflix Subscription', 649.00,'Entertainment','Monthly streaming',      CURDATE()),
    ('Doctor Visit',       800.00, 'Health',         'General checkup',       CURDATE());

INSERT INTO budgets (category, monthly_limit, month, year) VALUES
    ('General',       10000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    ('Food',           3000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    ('Bills',          5000.00, MONTH(CURDATE()), YEAR(CURDATE())),
    ('Entertainment',  1500.00, MONTH(CURDATE()), YEAR(CURDATE()));

-- ============================================================
--  Verification queries (run separately to check everything)
-- ============================================================
-- SELECT * FROM expenses;
-- SELECT * FROM budgets;
-- SELECT COUNT(*) FROM expenses;
