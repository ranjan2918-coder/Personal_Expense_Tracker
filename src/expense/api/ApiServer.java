package expense.api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Embedded REST API server built on Java's built-in HttpServer.
 *
 * Runs on http://localhost:8080
 *
 * Endpoints registered:
 *   GET    /api/expenses          – list all expenses
 *   GET    /api/expenses/{id}     – get expense by ID
 *   POST   /api/expenses          – add expense
 *   PUT    /api/expenses/{id}     – update expense
 *   DELETE /api/expenses/{id}     – delete expense
 *   GET    /api/budgets           – list all budgets
 *   POST   /api/budgets           – add budget
 *   GET    /api/health            – health check (for quick Postman ping)
 */
public class ApiServer {

    public static final int PORT = 8080;

    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Expense endpoints
        server.createContext("/api/expenses", new ExpenseHandler());

        // Budget endpoints
        server.createContext("/api/budgets", new BudgetHandler());

        // Health check
        server.createContext("/api/health", exchange -> {
            String body = "{\"status\":\"UP\",\"message\":\"Personal Expense Tracker API is running\"}";
            byte[] bytes = body.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // Use a thread pool so multiple requests don't block each other
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("============================================");
        System.out.println("  Expense Tracker REST API started");
        System.out.println("  URL: http://localhost:" + PORT);
        System.out.println("  Health check: http://localhost:" + PORT + "/api/health");
        System.out.println("============================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("[API] Server stopped.");
        }
    }
}
