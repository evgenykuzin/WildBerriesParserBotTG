import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseTest {

    @Test
    public void testIfNotExists() throws SQLException {
        Connection connection = initConnection(
                "jdbc:mysql://localhost:3306/test?serverTimezone=UTC",
                "root",
                "1357"
        );
        connection.prepareStatement("INSERT INTO test_table (product_name) values('value') ON DUPLICATE KEY UPDATE product_name = values(product_name) ").executeUpdate();
    }

    @Test
    public void testOnDuplicate() throws SQLException{
        Connection connection = initConnection(
                "jdbc:mysql://localhost:3306/test?serverTimezone=UTC",
                "root",
                "1357"
        );
        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_table (url, product_name, current_price, old_price, discount_percent) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE current_price = 0");
        ps.setString(1, "http://google.com");
        ps.setString(2, "google");
        ps.setDouble(3, 100);
        ps.setDouble(4, 200);
        ps.setDouble(5, 50);
        ps.executeUpdate();
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
