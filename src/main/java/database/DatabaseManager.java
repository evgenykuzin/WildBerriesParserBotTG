package database;

import com.mysql.cj.exceptions.ConnectionIsClosedException;
import entities.Product;
import exceptions.DBConnectionException;
import properties.PropertiesManager;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private final Connection connection;
    private String url;
    private String name;
    private String pass;
    private static final long reconnectingDBTime = 900000;
    private long waiting;
    private long lastCall;
    public DatabaseManager() {
        waiting = 0;
        lastCall = 0;
        loadProps();
        connection = initConnection(url, name, pass);
    }

    public DatabaseManager(Connection connection) {
        this.connection = connection;
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

    private void loadProps() {
        Properties dbProps = PropertiesManager.getProperties("db");
        url = dbProps.getProperty("db.url");
        name = dbProps.getProperty("db.name");
        pass = dbProps.getProperty("db.password");
    }

    public double getExistingProductPriceByUrl(String url) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM products WHERE url = ?");
            ps.setString(1, url);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("current_price");
            }
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException throwables) {
            waitingDatabase(reconnectingDBTime);
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public Map<String, Double> getAllExistingProductsMap() throws DBConnectionException{
        Map<String, Double> map = new HashMap<>();
        try {
            ResultSet resultSet = connection.prepareStatement(
                    "SELECT * FROM products").executeQuery();
            while (resultSet.next()) {
                String url = resultSet.getString("url");
                double price = resultSet.getDouble("current_price");
                map.put(url, price);
            }
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException throwables) {
            waitingDatabase(reconnectingDBTime);
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public void saveProduct(Product product) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO products (url, current_price) VALUES (?,?)");
            ps.setString(1, product.getUrl());
            ps.setString(2, String.valueOf(product.getNewPrice()));
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException throwables) {
            waitingDatabase(reconnectingDBTime);
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateProduct(Product product) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE products SET " +
                    "current_price = ? " +
                    "WHERE url = ?");
            ps.setDouble(1, product.getNewPrice());
            ps.setString(2, product.getUrl());
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Set<Product> getAllProducts() {
        Set<Product> set = new HashSet<>();
        try {
            ResultSet resultSet = connection.prepareStatement("SELECT * FROM products").executeQuery();
            while (resultSet.next()) {
                set.add(constructProduct(resultSet));
            }
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return set;
    }

    private Product constructProduct(ResultSet resultSet) throws SQLException {
        String url = resultSet.getString("url");
        String productName = resultSet.getString("product_name");
        String brandName = resultSet.getString("brand_name");
        double oldPrice = resultSet.getDouble("old_price");
        double currentPrice = resultSet.getDouble("current_price");
        double discountPercent = resultSet.getDouble("discount_percent");
        Product product = new Product(url);
        product.setProductName(productName);
        product.setBrandName(brandName);
        product.setNewPrice(currentPrice);
        product.setOldPrice(oldPrice);
        product.setDiscountPercent(discountPercent);
        return product;
    }

    public Set<String> getAllIgnoredBrands() {
        return getAll("ignore_brands", "brand");
    }

    public void saveIgnoredBrand(String brand) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO ignore_brands (brand) values(?)");
            ps.setString(1, brand);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeIgnoredBrand(String brand) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ignore_brands WHERE brand = ?");
            ps.setString(1, brand);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Set<String> getAllCategories() {
        return getAll("categories", "url");
    }

    public void saveCategory(String url) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO categories (url) values(?)");
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeCategory(String url) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM categories WHERE url = ?");
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Set<String> getAll(String table, String column) {
        Set<String> set = new HashSet<>();
        try {
            ResultSet resultSet = connection.prepareStatement("SELECT " + column + " FROM " + table).executeQuery();
            while (resultSet.next()) {
                set.add(resultSet.getString(column));
            }
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException ssee) {
            waitingDatabase(reconnectingDBTime);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return set;
    }

    public void waitingDatabase(long time) {
        if (!isWaiting()) {
            System.out.println("waiting database...");
            waiting = time;
            lastCall = System.currentTimeMillis();
        }
    }

    public boolean isWaiting() {
        if (System.currentTimeMillis() - lastCall > waiting) {
            lastCall = 0;
            waiting = 0;
            return false;
        } else {
            return true;
        }
    }

}
