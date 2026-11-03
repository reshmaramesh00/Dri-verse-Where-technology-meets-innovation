package Dri_verse.view;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple JDBC connection helper.
 * Update the URL / user / password for your MySQL (or other) database.
 * Add the appropriate JDBC driver jar to your classpath before running.
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/drivers_db";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}