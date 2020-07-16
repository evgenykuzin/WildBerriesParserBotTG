package bot;

import database.DatabaseManager;
import entities.Product;
import exceptions.DBConnectionException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.ShopParser;

import java.util.Map;
import java.util.Set;

public class Sender extends Thread {
    private final Bot bot;
    private final ShopParser shopParser;
    private final Set<String> categories;
    private final DatabaseManager databaseManager;
    private final Set<String> ignoredBrands;
    private Map<String, Double> savedProducts;
    private volatile Boolean running;
    private volatile long lastCall = 0;

    public Sender(Bot bot, ShopParser shopParser, DatabaseManager databaseManager) {
        this.bot = bot;
        this.shopParser = shopParser;
        this.databaseManager = databaseManager;
        categories = databaseManager.getAllCategories();
        ignoredBrands = databaseManager.getAllIgnoredBrands();
        try {
            savedProducts = databaseManager.getAllExistingProductsMap();
            if (savedProducts.isEmpty()) bot.sendText("products list is empty... try again");
        } catch (DBConnectionException throwables) {
            bot.sendText("Connection to database was lost. Wait ~15 minutes");
            throwables.printStackTrace();
        }
        running = Boolean.TRUE;
    }

    @Override
    public void run() throws OutOfMemoryError {
        System.out.println("restarted");
        bot.sendText("restarted");
        while (true) {
            if (running && !databaseManager.isWaiting()) {
                if (categories.isEmpty()) {
                    System.out.println("categories is empty(");
                    bot.sendText("categories is empty(\nuse 'cat_add' command to add categories\nstopping...");
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
                            saveProduct(parsedProduct);
                        } else {
                            compareAndUpdateProducts(parsedProduct, savedProductPrice);
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

    private void saveProduct(Product product) {
        try {
            databaseManager.saveProduct(product);
            savedProducts.put(product.getUrl(), product.getNewPrice());
        } catch (DBConnectionException throwables) {
            throwables.printStackTrace();
            return;
        }
        bot.sendText(product.constructMessage());
    }

    public void compareAndUpdateProducts(Product parsedProduct, double savedProductPrice) {
        double newDiscountPercent = 100 - (parsedProduct.getNewPrice() * 100 / savedProductPrice);
        boolean condition = parsedProduct.getNewPrice() < savedProductPrice;
        if (condition) {
            parsedProduct.setOldPrice(savedProductPrice);
            parsedProduct.setDiscountPercent(newDiscountPercent);
            try {
                databaseManager.updateProduct(parsedProduct);
                savedProducts.replace(parsedProduct.getUrl(), parsedProduct.getNewPrice());
            } catch (DBConnectionException throwables) {
                throwables.printStackTrace();
            }
            bot.sendText(parsedProduct.constructMessage());
        }
    }

    public void addCategory(String brand) {
        ignoredBrands.add(brand);
    }

    public void removeCategory(String brand) {
        ignoredBrands.remove(brand);
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
