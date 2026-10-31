package expense.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import expense.dao.BudgetDAO;
import expense.model.Budget;
import expense.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * HTTP handler for /api/budgets and /api/budgets/{id}
 *
 * Routes:
 *   GET  /api/budgets      → getAllBudgets()
 *   POST /api/budgets      → addBudget()
 */
public class BudgetHandler implements HttpHandler {

    private final BudgetDAO dao = new BudgetDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath();

        String suffix = path.substring("/api/budgets".length());
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
                    addBudget(exchange);
                    break;
                case "PUT":
                    if (!hasId) { sendResponse(exchange, 400, JsonUtil.error("ID required for PUT")); return; }
                    updateBudget(exchange, id);
                    break;
                case "DELETE":
                    if (!hasId) { sendResponse(exchange, 400, JsonUtil.error("ID required for DELETE")); return; }
                    deleteBudget(exchange, id);
                    break;
                default:
                    sendResponse(exchange, 405, JsonUtil.error("Method Not Allowed"));
            }
        } catch (Exception e) {
            System.err.println("[BudgetHandler] Error: " + e.getMessage());
            sendResponse(exchange, 500, JsonUtil.error("Internal server error: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // GET /api/budgets
    // ------------------------------------------------------------------
    private void getAll(HttpExchange exchange) throws Exception {
        List<Budget> list = dao.getAllBudgets();
        sendResponse(exchange, 200, JsonUtil.budgetsToJsonArray(list));
    }

    // ------------------------------------------------------------------
    // GET /api/budgets/{id}
    // ------------------------------------------------------------------
    private void getById(HttpExchange exchange, int id) throws Exception {
        Budget b = dao.getBudgetById(id);
        if (b == null) {
            sendResponse(exchange, 404, JsonUtil.error("Budget not found with id " + id));
        } else {
            sendResponse(exchange, 200, JsonUtil.toJson(b));
        }
    }

    // ------------------------------------------------------------------
    // POST /api/budgets
    // ------------------------------------------------------------------
    private void addBudget(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);

        String category = JsonUtil.parseString(body, "category");
        double limit    = JsonUtil.parseDouble(body, "monthly_limit", -1);
        int    month    = JsonUtil.parseInt(body,    "month",
                java.time.LocalDate.now().getMonthValue());
        int    year     = JsonUtil.parseInt(body,    "year",
                java.time.LocalDate.now().getYear());

        if (category == null || category.trim().isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.error("Field 'category' is required")); return;
        }
        if (limit <= 0) {
            sendResponse(exchange, 400, JsonUtil.error("Field 'monthly_limit' must be a positive number")); return;
        }
        if (month < 1 || month > 12) {
            sendResponse(exchange, 400, JsonUtil.error("'month' must be between 1 and 12")); return;
        }
        if (year < 2000 || year > 2100) {
            sendResponse(exchange, 400, JsonUtil.error("'year' must be a valid 4-digit year")); return;
        }

        Budget budget = new Budget(0, category.trim(), limit, month, year);
        int newId = dao.addBudget(budget);
        sendResponse(exchange, 201, JsonUtil.created("Budget added successfully", newId));
    }

    // ------------------------------------------------------------------
    // PUT /api/budgets/{id}
    // ------------------------------------------------------------------
    private void updateBudget(HttpExchange exchange, int id) throws Exception {
        Budget existing = dao.getBudgetById(id);
        if (existing == null) {
            sendResponse(exchange, 404, JsonUtil.error("Budget not found with id " + id)); return;
        }

        String body     = readBody(exchange);
        String category = JsonUtil.parseString(body, "category");
        double limit    = JsonUtil.parseDouble(body, "monthly_limit", existing.getMonthlyLimit());
        int    month    = JsonUtil.parseInt(body,    "month",  existing.getMonth());
        int    year     = JsonUtil.parseInt(body,    "year",   existing.getYear());

        if (category == null || category.trim().isEmpty()) category = existing.getCategory();
        if (limit <= 0) {
            sendResponse(exchange, 400, JsonUtil.error("'monthly_limit' must be positive")); return;
        }

        Budget updated = new Budget(id, category.trim(), limit, month, year);
        dao.updateBudget(updated);
        sendResponse(exchange, 200, JsonUtil.toJson(updated));
    }

    // ------------------------------------------------------------------
    // DELETE /api/budgets/{id}
    // ------------------------------------------------------------------
    private void deleteBudget(HttpExchange exchange, int id) throws Exception {
        boolean deleted = dao.deleteBudget(id);
        if (deleted) {
            sendResponse(exchange, 200, JsonUtil.success("Budget " + id + " deleted successfully"));
        } else {
            sendResponse(exchange, 404, JsonUtil.error("Budget not found with id " + id));
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
