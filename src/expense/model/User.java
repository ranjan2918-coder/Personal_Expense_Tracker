package expense.model;

/**
 * User model – represents an authenticated user stored in the `users` table.
 */
public class User {
    private int    userId;
    private String username;
    private String password; // stored as plain text (sufficient for college project)

    public User(int userId, String username, String password) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
    }

    public int    getUserId()  { return userId; }
    public String getUsername(){ return username; }
    public String getPassword(){ return password; }

    public void setUserId(int userId)      { this.userId = userId; }
    public void setUsername(String u)      { this.username = u; }
    public void setPassword(String p)      { this.password = p; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "'}";
    }
}
