package expense.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import expense.dao.ExpenseDAO;
import expense.model.Expense;
import expense.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * HTTP handler for /api/expenses and /api/expenses/{id}
 *
 * Routes:
 *   GET    /api/expenses          → getAllExpenses()
 *   GET    /api/expenses/{id}     → getExpenseById()
 *   POST   /api/expenses          → addExpense()
 *   PUT    /api/expenses/{id}     → updateExpense()
 *   DELETE /api/expenses/{id}     → deleteExpense()
 */
public class ExpenseHandler implements HttpHandler {

    private final ExpenseDAO dao = new ExpenseDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers so Postman / browser clients work without issues
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        // Pre-flight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath(); // e.g. /api/expenses/3

        // Extract optional trailing ID  →  /api/expenses/3  →  "3"
        String suffix = path.substring("/api/expenses".length());
        boolean hasId = suffix.startsWith("/") && suffix.length() > 1;
        int id = -1;
        if (hasId) {
            try {
                id = Integer.parseInt(suffix.substring(1));
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, JsonUtil.error("Invalid ID in URL"));
                return;
            }
        }

        try {
            switch (method) {
                case "GET":
                    if (hasId) getById(exchange, id);
                    else       getAll(exchange);
                    break;
                case "POST":
                    addExpense(exchange);
                    break;
                case "PUT":
                    if (!hasId) { sendResponse(exchange, 400, JsonUtil.error("ID required for PUT")); return; }
                    updateExpense(exchange, id);
                    break;
                case "DELETE":
                    if (!hasId) { sendResponse(exchange, 400, JsonUtil.error("ID required for DELETE")); return; }
                    deleteExpense(exchange, id);
                    break;
                default:
                    sendResponse(exchange, 405, JsonUtil.error("Method Not Allowed"));
            }
        } catch (Exception e) {
            System.err.println("[ExpenseHandler] Error: " + e.getMessage());
            sendResponse(exchange, 500, JsonUtil.error("Internal server error: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // GET /api/expenses
    // ------------------------------------------------------------------
    private void getAll(HttpExchange exchange) throws Exception {
        List<Expense> list = dao.getAllExpenses();
        sendResponse(exchange, 200, JsonUtil.toJsonArray(list));
    }

    // ------------------------------------------------------------------
    // GET /api/expenses/{id}
    // ------------------------------------------------------------------
    private void getById(HttpExchange exchange, int id) throws Exception {
        Expense e = dao.getExpenseById(id);
        if (e == null) {
            sendResponse(exchange, 404, JsonUtil.error("Expense not found with id " + id));
        } else {
            sendResponse(exchange, 200, JsonUtil.toJson(e));
        }
    }

    // ------------------------------------------------------------------
    // POST /api/expenses
    // ------------------------------------------------------------------
    private void addExpense(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);

        String title       = JsonUtil.parseString(body, "title");
        String category    = JsonUtil.parseString(body, "category");
        String description = JsonUtil.parseString(body, "description");
        double amount      = JsonUtil.parseDouble(body, "amount", -1);
        LocalDate date     = JsonUtil.parseDate(body,   "expense_date");

        // Validation
        if (title == null || title.trim().isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.error("Field 'title' is required")); return;
        }
        if (category == null || category.trim().isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.error("Field 'category' is required")); return;
        }
        if (amount <= 0) {
            sendResponse(exchange, 400, JsonUtil.error("Field 'amount' must be a positive number")); return;
        }
        if (date == null) {
            sendResponse(exchange, 400, JsonUtil.error("Field 'expense_date' is required (yyyy-MM-dd)")); return;
        }
        if (date.isAfter(LocalDate.now())) {
            sendResponse(exchange, 400, JsonUtil.error("'expense_date' cannot be in the future")); return;
        }

        Expense expense = new Expense(0, title.trim(), category.trim(), amount,
                description == null ? "" : description.trim(), date);
        int newId = dao.addExpense(expense);
        sendResponse(exchange, 201, JsonUtil.created("Expense added successfully", newId));
    }

    // ------------------------------------------------------------------
    // PUT /api/expenses/{id}
    // ------------------------------------------------------------------
    private void updateExpense(HttpExchange exchange, int id) throws Exception {
        Expense existing = dao.getExpenseById(id);
        if (existing == null) {
            sendResponse(exchange, 404, JsonUtil.error("Expense not found with id " + id)); return;
        }

        String body = readBody(exchange);

        // Use existing values as defaults if a field is omitted
        String title       = JsonUtil.parseString(body, "title");
        String category    = JsonUtil.parseString(body, "category");
        String description = JsonUtil.parseString(body, "description");
        double amount      = JsonUtil.parseDouble(body, "amount", existing.getAmount());
        LocalDate date     = JsonUtil.parseDate(body,   "expense_date");

        if (title   == null || title.trim().isEmpty())    title    = existing.getTitle();
        if (category== null || category.trim().isEmpty()) category = existing.getCategory();
        if (description == null) description = existing.getDescription();
        if (date == null) date = existing.getExpenseDate();

        if (amount <= 0) {
            sendResponse(exchange, 400, JsonUtil.error("'amount' must be positive")); return;
        }
        if (date.isAfter(LocalDate.now())) {
            sendResponse(exchange, 400, JsonUtil.error("'expense_date' cannot be in the future")); return;
        }

        Expense updated = new Expense(id, title.trim(), category.trim(), amount,
                description.trim(), date);
        dao.updateExpense(updated);
        sendResponse(exchange, 200, JsonUtil.toJson(updated));
    }

    // ------------------------------------------------------------------
    // DELETE /api/expenses/{id}
    // ------------------------------------------------------------------
    private void deleteExpense(HttpExchange exchange, int id) throws Exception {
        boolean deleted = dao.deleteExpense(id);
        if (deleted) {
            sendResponse(exchange, 200, JsonUtil.success("Expense " + id + " deleted successfully"));
        } else {
            sendResponse(exchange, 404, JsonUtil.error("Expense not found with id " + id));
        }
    }

    // ------------------------------------------------------------------
    // Utilities
    // ------------------------------------------------------------------
    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toString("UTF-8");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
