import database.DatabaseManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class DatabaseTest {

    @Test
    public void testConnectionException() throws SQLException {
        DatabaseManager databaseManager = new DatabaseManager(initConnection(
                "jdbc:mysql://localhost:3306/test?serverTimezone=UTC",
                "root",
                "1357"
        ));
        databaseManager.waitingDatabase(10000);
        while (true) {
            if (databaseManager.isWaiting()) {
              System.out.println("waiting");
            } else {
               System.out.println("working");
            }
        }
    }

    private Connection initConnection(String url, String name, String pass) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, name, pass);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return conn;
    }
}
