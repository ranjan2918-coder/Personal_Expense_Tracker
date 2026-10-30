# Personal Expense Tracker Using Java

A modular Java 8 project with a **Swing GUI** front-end.

## Requirements
- JDK 8 or later
- Eclipse IDE (or IntelliJ IDEA, or command line)

## Package structure
- `expense.model` – Expense, Budget, Reportable
- `expense.service` – Expense business logic and reports
- `expense.exception` – Custom validation exception
- `expense.util` – File storage (expenses.txt)
- `expense.app` – Console menu (original)
- `expense.gui` – **Swing GUI front-end** (new)

## GUI classes (`expense.gui`)
| Class               | Purpose                                           |
|---------------------|---------------------------------------------------|
| `ExpenseTrackerApp` | **Main class** – launch this to start the GUI     |
| `LoginScreen`       | Login window (default: admin / admin)             |
| `MainFrame`         | Main window with navigation bar & CardLayout      |
| `DashboardPanel`    | Summary statistics & category breakdown           |
| `AddExpensePanel`   | Form to add a new expense                         |
| `ViewExpensesPanel` | Table of all expenses + Update / Delete / Sort    |
| `SearchExpensePanel`| Search expenses by category                       |
| `MonthlyReportPanel`| Monthly expense report with category breakdown    |
| `BudgetPanel`       | Set and monitor monthly budget                    |
| `AppTheme`          | Centralized colours, fonts and borders            |
| `ComponentFactory`  | Factory for styled Swing components               |

## Run in Eclipse (GUI version)
1. Open the project in Eclipse (`File → Open Projects from File System`).
2. Navigate to `expense.gui.ExpenseTrackerApp` in the Package Explorer.
3. Right-click → **Run As → Java Application**.
4. Login with **admin / admin**.
5. Use the navigation bar to switch between screens.

## Run from command line
```bash
javac -d bin -encoding UTF-8 src/expense/model/*.java src/expense/exception/*.java src/expense/util/*.java src/expense/service/*.java src/expense/app/*.java src/expense/gui/*.java
java -cp bin expense.gui.ExpenseTrackerApp
```

## Console version (original)
The original console menu is still available:
```bash
java -cp bin expense.app.PersonalExpenseTracker
```

## Data storage
Expense records are stored in `expenses.txt` (pipe-delimited) in the working directory.
The monthly budget is held in memory and defaults to ₹10,000 on each restart.
