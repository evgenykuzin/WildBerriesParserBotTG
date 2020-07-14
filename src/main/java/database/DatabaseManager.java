package database;

import entities.Product;
import properties.PropertiesManager;

import java.sql.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class DatabaseManager {
    private final Connection connection;
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
        Properties dbProps = PropertiesManager.getProperties("database");
        url = dbProps.getProperty("db.url");
        name = dbProps.getProperty("db.name");
        pass = dbProps.getProperty("db.password");
    }

    public Product getExistingProductByUrl(String url) {
        Product product = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM products WHERE url = ?");
            ps.setString(1, url);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                product = constructProduct(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    public void saveProduct(Product product) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO products (url, product_name, brand_name, current_price, old_price, discount_percent) values (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE products.current_price = ?, products.old_price = ?, products.discount_percent = ?");
            ps.setString(1, product.getUrl());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getBrandName());
            ps.setString(4, String.valueOf(product.getNewPrice()));
            ps.setString(5, String.valueOf(product.getOldPrice()));
            ps.setString(6, String.valueOf(product.getDiscountPercent()));
            ps.setString(7, String.valueOf(product.getNewPrice()));
            ps.setString(8, String.valueOf(product.getOldPrice()));
            ps.setString(9, String.valueOf(product.getDiscountPercent()));
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
            ps.setDouble(1, product.getNewPrice());
            ps.setDouble(2, product.getOldPrice());
            ps.setDouble(3, product.getDiscountPercent());
            ps.setString(4, product.getUrl());
            ps.executeUpdate();
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return set;
    }

    private Product constructProduct(ResultSet resultSet) throws SQLException{
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeIgnoredBrand(String brand) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ignore_brands WHERE brand = ?");
            ps.setString(1, brand);
            ps.executeUpdate();
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeCategory(String url) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM categories WHERE url = ?");
            ps.setString(1, url);
            ps.executeUpdate();
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return set;
    }

}
