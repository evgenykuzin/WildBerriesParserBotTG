package bot;

import com.mysql.cj.exceptions.ConnectionIsClosedException;
import database.DatabaseManager;
import entities.Product;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.ShopParser;

import java.util.Set;

public class Sender extends Thread {
    private final Bot bot;
    private final ShopParser shopParser;
    private final Set<String> categories;
    private final DatabaseManager databaseManager;
    private volatile Boolean running;
    private volatile long lastCall = 0;

    public Sender(Bot bot, ShopParser shopParser, Set<String> categories, DatabaseManager databaseManager) {
        this.bot = bot;
        this.shopParser = shopParser;
        this.categories = categories;
        this.databaseManager = databaseManager;
        running = Boolean.TRUE;
    }

    @Override
    public void run() throws OutOfMemoryError, ConnectionIsClosedException {
        bot.sendText("start parsing...");
        while (true) {
            if (running) {
                if (categories.isEmpty()) {
                    bot.sendText("categories is empty(");
                    running = Boolean.FALSE;
                }
                for (String url : categories) {
                    if (!running) {
                        bot.sendText("stopped!");
                        break;
                    }
                    Elements category = shopParser.parseCategory(url);
                    for (Element element : category) {
                        if (!running) break;
                        Product parsedProduct = shopParser.parseProduct(element, databaseManager.getAllIgnoredBrands());
                        if (parsedProduct == null) continue;
                        Product savedProduct = databaseManager.getExistingProductByUrl(parsedProduct.getUrl());
                        if (savedProduct == null) {
                            saveProduct(parsedProduct);
                        } else {
                            compareAndUpdateProducts(parsedProduct, savedProduct);
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
        boolean condition = product.getNewPrice() <= 100 ||
                product.getDiscountPercent() >= 85;
        condition = true;
        if (condition) {
            bot.sendText(product.constructMessage());
            databaseManager.saveProduct(product);
        }
    }

    public void compareAndUpdateProducts(Product parsedProduct, Product savedProduct) {
        double newDiscountPercent = 100 - (parsedProduct.getNewPrice() * 100 / savedProduct.getNewPrice());
        boolean condition = newDiscountPercent >= 85
                || parsedProduct.getNewPrice() < savedProduct.getNewPrice()
                && parsedProduct.getNewPrice() <= 100;
        condition = parsedProduct.getNewPrice() < savedProduct.getNewPrice();
        if (condition) {
            parsedProduct.setOldPrice(savedProduct.getNewPrice());
            parsedProduct.setDiscountPercent(newDiscountPercent);
            databaseManager.updateProduct(parsedProduct);
            bot.sendText(parsedProduct.constructMessage());
        }
    }

    public Boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(Boolean running) {
        this.running = running;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
