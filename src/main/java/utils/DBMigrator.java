package utils;

import database.DatabaseManager;
import exceptions.DBConnectionException;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DBMigrator {
    private static final DatabaseManager databaseManager = new DatabaseManager();
    private static final String categories = "categories";
    private static final String ignore_products = "ignore_products";
    private static final String products = "products";
    private static DatabaseManager fromDatabaseManager;
    public static void main(String[] args) throws Exception {
//        fromDatabaseManager = new DatabaseManager(getConnection(
//                "",
//                "",
//                ""
//        ));
        fromDatabaseManager = databaseManager;
        //migrateCategoriesFromFile(new File("C:\\Users\\JekaJops\\IntelliJIDEAProjects\\WildBerriesParserBotTG\\src\\main\\resources\\files\\categoryLinks.txt"));
        //migrateIgnoredBrandsFromFile(new File(""));
        migrateCategoriesFromDB();
    }

    public static void migrateCategoriesFromFile(File file) throws Exception {
        migrate(databaseManager.getAllCategories(), databaseManager::saveCategory, new FileLoader(file));
    }

    public static void migrateIgnoredBrandsFromFile(File file) throws Exception {
        migrate(databaseManager.getAllIgnoredBrands(), databaseManager::saveIgnoredBrand, new FileLoader(file));
    }

    public static void migrateCategoriesFromDB() throws Exception {

        migrate(
                fromDatabaseManager.getAllCategories(),
                databaseManager::saveCategory,
                new DBLoader<String>(categories, fromDatabaseManager)
        );
    }

    public static void migrate(Set<String> existing, Saver saver, Loader loader) throws Exception {
        Collection<String> lines = loader.load();
        lines.forEach(line -> {
            if (!existing.contains(line)) {
                try {
                    saver.save(line);
                } catch (DBConnectionException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private static class FileLoader implements Loader<String> {
        File file;

        public FileLoader(File file) {
            this.file = file;
        }

        @Override
        public Collection<String> load() throws Exception {
            return (Files.readAllLines(file.toPath()));
        }
    }

    private static class DBLoader<T> implements Loader<T> {
        DatabaseManager fromDatabaseManager;
        String table;

        public DBLoader(String table, DatabaseManager fromDatabaseManager) throws Exception {
            this.table = table;
            this.fromDatabaseManager = fromDatabaseManager;
        }

        @Override
        public Collection<String> load() throws Exception {
            if (table.equals("categories")) return (databaseManager.getAllCategories());
            if (table.equals("ignore_brands")) return (databaseManager.getAllIgnoredBrands());
            Collection<String> products = new HashSet<>();
            databaseManager.getAllProducts().forEach(p -> products.add(p.constructMessage()));
            if (table.equals("products")) return (products);
            return Collections.emptySet();
        }


    }

    private static Connection getConnection(String url, String name, String pass) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, name, pass);
    }

    private interface Saver {
        void save(String saved) throws DBConnectionException;
    }

    private interface Loader<T> {
        Collection<String> load() throws Exception;
    }
}
