package database;

import com.mysql.cj.exceptions.ConnectionIsClosedException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import entities.Product;
import exceptions.DBConnectionException;
import properties.PropertiesManager;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private Connection connection;
    private String url;
    private String name;
    private String pass;
    public DatabaseManager() {
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
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public void saveProduct(Product product) throws DBConnectionException, SQLIntegrityConstraintViolationException {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO products (url, current_price) VALUES (?,?)");
            ps.setString(1, product.getUrl());
            ps.setString(2, String.valueOf(product.getNewPrice()));
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLIntegrityConstraintViolationException sicve) {
            sicve.printStackTrace();
            throw sicve;
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
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Set<String> getAllIgnoredBrands() throws DBConnectionException {
        return getAll("ignore_brands", "brand");
    }

    public void saveIgnoredBrand(String brand) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO ignore_brands (brand) values(?)");
            ps.setString(1, brand);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeIgnoredBrand(String brand) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ignore_brands WHERE brand = ?");
            ps.setString(1, brand);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void clearIgnoredBrands() throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement("TRUNCATE TABLE ignore_brands");
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void clearCategories() throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement("TRUNCATE TABLE categories");
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Set<String> getAllCategories() throws DBConnectionException{
        return getAll("categories", "url");
    }

    public void saveCategory(String url) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO categories (url) values(?)");
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeCategory(String url) throws DBConnectionException {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM categories WHERE url = ?");
            ps.setString(1, url);
            ps.executeUpdate();
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private Set<String> getAll(String table, String column) throws DBConnectionException {
        Set<String> set = new HashSet<>();
        try {
            ResultSet resultSet = connection.prepareStatement("SELECT " + column + " FROM " + table).executeQuery();
            while (resultSet.next()) {
                set.add(resultSet.getString(column));
            }
        } catch (SQLSyntaxErrorException | SQLNonTransientConnectionException | ConnectionIsClosedException | CommunicationsException throwables) {
            throwables.printStackTrace();
            throw new DBConnectionException();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return set;
    }

    public void reconnect() {
        connection = initConnection(url, name, pass);
    }

}
