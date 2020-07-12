package database;

import entities.Product;
import properties.PropertiesManager;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private Connection connection;
    private String url;
    private String name;
    private String pass;

    public DatabaseManager() {
        loadProps();
        connection = initConnection(url, name, pass);
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
        Properties dbProps = PropertiesManager.getProperties("databasetest");
        url = dbProps.getProperty("db.url");
        name = dbProps.getProperty("db.name");
        pass = dbProps.getProperty("db.password");
    }

    public Product getExistingEntityByUrl(String url) {
        Product product = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM products WHERE url = ?");
            ps.setString(1, url);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String productName = resultSet.getString("product_name");
                String brandName = resultSet.getString("brand_name");
                double oldPrice = Double.parseDouble(resultSet.getString("old_price"));
                double currentPrice = Double.parseDouble(resultSet.getString("current_price"));
                double discountPercent = Double.parseDouble(resultSet.getString("discount_percent"));
                product = new Product(url);
                product.setProductName(productName);
                product.setBrandName(brandName);
                product.setNewPrice(currentPrice);
                product.setOldPrice(oldPrice);
                product.setDiscountPercent(discountPercent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    public void saveProduct(Product product) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO products (url, product_name, brand_name, current_price, old_price, discount_percent) values (?,?,?,?,?,?)");
            ps.setString(1, product.getUrl());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getBrandName());
            ps.setString(4, String.valueOf(product.getNewPrice()));
            ps.setString(5, String.valueOf(product.getOldPrice()));
            ps.setString(6, String.valueOf(product.getDiscountPercent()));
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateProduct(Product product) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE products SET " +
                    "current_price = ?," +
                    "old_price = ?," +
                    "discount_percent = ?" +
                    "WHERE url = ?");
            ps.setString(1, String.valueOf(product.getNewPrice()));
            ps.setString(2, String.valueOf(product.getOldPrice()));
            ps.setString(3, String.valueOf(product.getDiscountPercent()));
            ps.setString(4, product.getUrl());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
