package expense.util;

import expense.model.Budget;
import expense.model.Expense;

import java.time.LocalDate;
import java.util.List;

/**
 * Minimal hand-written JSON helper.
 *
 * Keeps the project free of external JSON libraries while still supporting
 * everything needed by the REST API handlers.
 *
 * Supported operations:
 *   - Serialize Expense / Budget objects to JSON strings
 *   - Parse individual field values from a simple flat JSON body
 *
 * NOTE: This parser handles only flat JSON objects (no nested objects or
 * arrays in the request body).  It is intentionally simple because the
 * request payloads for this application are small and well-defined.
 */
public final class JsonUtil {

    private JsonUtil() { }

    // ================================================================
    //  SERIALIZERS
    // ================================================================

    /** Converts an Expense to a JSON object string. */
    public static String toJson(Expense e) {
        return "{"
                + "\"id\":"          + e.getId()                   + ","
                + "\"title\":"       + quote(e.getTitle())         + ","
                + "\"amount\":"      + e.getAmount()               + ","
                + "\"category\":"    + quote(e.getCategory())      + ","
                + "\"description\":" + quote(e.getDescription())   + ","
                + "\"expense_date\":" + quote(e.getExpenseDate().toString())
                + "}";
    }

    /** Converts a list of Expense objects to a JSON array string. */
    public static String toJsonArray(List<Expense> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Converts a Budget to a JSON object string. */
    public static String toJson(Budget b) {
        return "{"
                + "\"id\":"            + b.getId()            + ","
                + "\"category\":"      + quote(b.getCategory())      + ","
                + "\"monthly_limit\":" + b.getMonthlyLimit()         + ","
                + "\"month\":"         + b.getMonth()                + ","
                + "\"year\":"          + b.getYear()
                + "}";
    }

    /** Converts a list of Budget objects to a JSON array string. */
    public static String budgetsToJsonArray(List<Budget> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Wraps a message string in a simple JSON error object. */
    public static String error(String message) {
        return "{\"error\":" + quote(message) + "}";
    }

    /** Wraps a message string in a simple JSON success object. */
    public static String success(String message) {
        return "{\"message\":" + quote(message) + "}";
    }

    /** Wraps a message + id in a JSON created object. */
    public static String created(String message, int id) {
        return "{\"message\":" + quote(message) + ",\"id\":" + id + "}";
    }

    // ================================================================
    //  PARSERS  (flat JSON only)
    // ================================================================

    /**
     * Extracts the string value for the given key from a flat JSON body.
     * Returns null if the key is absent.
     *
     * Works for:  {"key":"value", ...}  and  {"key":null, ...}
     */
    public static String parseString(String json, String key) {
        // Match:  "key" : "value"
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;

        int colonIdx = json.indexOf(':', keyIdx + searchKey.length());
        if (colonIdx < 0) return null;

        // Skip whitespace after colon
        int valStart = colonIdx + 1;
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
            valStart++;
        }
        if (valStart >= json.length()) return null;

        char first = json.charAt(valStart);
        if (first == '"') {
            // Quoted string value – find closing quote (handle escaped quotes)
            StringBuilder sb = new StringBuilder();
            int i = valStart + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    sb.append(next);
                    i += 2;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    i++;
                }
            }
            return sb.toString();
        } else if (json.startsWith("null", valStart)) {
            return null;
        }
        // Unquoted value (number / boolean) – read until delimiter
        int end = valStart;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
            end++;
        }
        return json.substring(valStart, end);
    }

    /** Parses a double field from flat JSON. Returns defaultValue if absent or invalid. */
    public static double parseDouble(String json, String key, double defaultValue) {
        String raw = parseString(json, key);
        if (raw == null || raw.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Parses an int field from flat JSON. Returns defaultValue if absent or invalid. */
    public static int parseInt(String json, String key, int defaultValue) {
        String raw = parseString(json, key);
        if (raw == null || raw.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Parses a LocalDate field (yyyy-MM-dd) from flat JSON. Returns null if absent / invalid. */
    public static LocalDate parseDate(String json, String key) {
        String raw = parseString(json, key);
        if (raw == null || raw.isEmpty()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // ================================================================
    //  Private helpers
    // ================================================================

    private static String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
               + "\"";
    }
}
