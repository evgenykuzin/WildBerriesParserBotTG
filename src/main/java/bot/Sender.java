package bot;

import database.DatabaseManager;
import entities.Product;
import exceptions.DBConnectionException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.ShopParser;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class Sender extends Thread {
    private final Bot bot;
    private final ShopParser shopParser;
    private Set<String> categories;
    private final DatabaseManager databaseManager;
    private Set<String> ignoredBrands;
    private Map<String, Double> savedProducts;
    private final Queue<Product> backupSavedProducts;
    private final Queue<Product> backupUpdatedProducts;
    private volatile Boolean running;
    private volatile long lastCall = 0;

    public Sender(Bot bot, ShopParser shopParser, DatabaseManager databaseManager) {
        this.bot = bot;
        this.shopParser = shopParser;
        this.databaseManager = databaseManager;
        backupSavedProducts = new ArrayBlockingQueue<>(20);
        backupUpdatedProducts = new ArrayBlockingQueue<>(20);
        try {
            categories = databaseManager.getAllCategories();
            ignoredBrands = databaseManager.getAllIgnoredBrands();
            savedProducts = databaseManager.getAllExistingProductsMap();
            if (savedProducts.isEmpty()) bot.sendText("products list is empty...");
            if (savedProducts.isEmpty()) bot.sendText("ignored brands list is empty...");
            if (savedProducts.isEmpty()) bot.sendText("categories list is empty...");
        } catch (DBConnectionException throwables) {
            bot.sendText("connection problem... failed to load data from database");
            System.out.println("connection problem... failed to load data from database");
            databaseManager.reconnect();
            throwables.printStackTrace();
        }
        running = Boolean.TRUE;
    }

    @Override
    public void run() throws OutOfMemoryError {
        System.out.println("restarted");
        bot.sendText("restarted");
        while (true) {
            if (running) {
                if (categories.isEmpty()) {
                    System.out.println("categories list is empty(");
                    bot.sendText("categories list is empty(\nuse 'cat_add' command to add categories\nstopping...");
                    running = Boolean.FALSE;
                    continue;
                }
                for (String url : categories) {
                    if (!running) {
                        System.out.println("sender stopped");
                        bot.sendText("stopped!");
                        break;
                    }
                    Elements category = shopParser.parseCategory(url);
                    for (Element element : category) {
                        if (!running) break;
                        Product parsedProduct = shopParser.parseProduct(element, ignoredBrands);
                        if (parsedProduct == null) continue;
                        Double savedProductPrice = savedProducts.get(parsedProduct.getUrl());
                        if (savedProductPrice == null) {
                            if (!backupSavedProducts.contains(parsedProduct)) {
                                saveProductToDatabase(parsedProduct);
                            }
                        } else {
                            if (!backupUpdatedProducts.contains(parsedProduct)) {
                                compareAndUpdateProducts(parsedProduct, savedProductPrice);
                            }
                        }
                        waiting();
                    }
                }
            }
        }
    }

    private void waiting() {
        if (System.currentTimeMillis() - lastCall > 5000) {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lastCall = System.currentTimeMillis();
        }
    }

    public void backup() throws DBConnectionException, SQLIntegrityConstraintViolationException {
        while (!backupSavedProducts.isEmpty()) {
            Product p = backupSavedProducts.poll();
            saveProduct(p);
            System.out.println("product " + p + " saved from backup");
        }
        while (!backupUpdatedProducts.isEmpty()) {
            Product p = backupUpdatedProducts.poll();
            updateProduct(p);
            System.out.println("product " + p + " updated from backup");
        }
    }

    private void saveProductToDatabase(Product product) {
        boolean duplicate = false;
        try {
            saveProduct(product);
            backup();
        } catch (DBConnectionException throwables) {
            if (!backupSavedProducts.contains(product)) {
                backupSavedProducts.offer(product);
            }
            //bot.sendText("connection problem... failed to save");
            System.out.println("connection problem... failed to save product " + product);
            databaseManager.reconnect();
            throwables.printStackTrace();
        } catch (SQLIntegrityConstraintViolationException sicve) {
            duplicate = sicve.getMessage().contains("Duplicate");
        } finally {
            if (!duplicate) bot.sendText(product.constructMessage());
        }
    }

    private void saveProduct(Product product) throws DBConnectionException, SQLIntegrityConstraintViolationException {
        databaseManager.saveProduct(product);
        savedProducts.put(product.getUrl(), product.getNewPrice());
    }

    public void compareAndUpdateProducts(Product parsedProduct, double savedProductPrice) {
        double parsedProductPrice = parsedProduct.getNewPrice();
        double newDiscountPercent = 100 - (parsedProductPrice * 100 / savedProductPrice);
        boolean condition = parsedProductPrice < savedProductPrice;
        if (condition) {
            parsedProduct.setOldPrice(savedProductPrice);
            parsedProduct.setDiscountPercent(newDiscountPercent);
            try {
                updateProduct(parsedProduct);
                backup();
            } catch (DBConnectionException throwables) {
                if (!backupUpdatedProducts.contains(parsedProduct)) {
                    backupUpdatedProducts.offer(parsedProduct);
                }
                //bot.sendText("connection problem... failed to update");
                System.out.println("connection problem... failed to update product " + parsedProduct);
                databaseManager.reconnect();
                throwables.printStackTrace();
            } catch (SQLIntegrityConstraintViolationException sicve) {
                sicve.printStackTrace();
            } finally {
                bot.sendText(parsedProduct.constructMessage());
            }
        }
    }

    private void updateProduct(Product product) throws DBConnectionException {
        databaseManager.updateProduct(product);
        savedProducts.replace(product.getUrl(), product.getNewPrice());
    }

    public void addCategory(String brand) {
        categories.add(brand);
    }

    public void removeCategory(String brand) {
        categories.remove(brand);
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void addIgnoredBrand(String brand) {
        ignoredBrands.add(brand);
    }

    public void removeIgnoredBrand(String brand) {
        ignoredBrands.remove(brand);
    }

    public Set<String> getIgnoredBrands() {
        return ignoredBrands;
    }

    public Boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(Boolean running) {
        this.running = running;
    }


}
