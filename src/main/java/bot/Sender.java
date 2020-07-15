package bot;

import com.mysql.cj.exceptions.ConnectionIsClosedException;
import database.DatabaseManager;
import entities.Product;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parser.ShopParser;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
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
        System.out.println("restarted");
        bot.sendText("restarted");
        while (true) {
            if (running) {
                if (categories.isEmpty()) {
                    System.out.println("categories is empty(");
                    bot.sendText("categories is empty(");
                    running = Boolean.FALSE;
                    bot.sendText("stopping parsing");
                    bot.sendText("use 'cat_add' command to add categories");
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
                        Product parsedProduct = shopParser.parseProduct(element, databaseManager.getAllIgnoredBrands());
                        if (parsedProduct == null) continue;
                        double savedProductPrice;
                        try {
                            savedProductPrice = databaseManager.getExistingProductPriceByUrl(parsedProduct.getUrl());
                        } catch (SQLException throwables) {
                            continue;
                        }
                        if (savedProductPrice == -1) {
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
        } catch (SQLException throwables) {
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
            } catch (SQLSyntaxErrorException throwables) {
                throwables.printStackTrace();
            }
            bot.sendText(parsedProduct.constructMessage());
        }
    }

    public Boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(Boolean running) {
        this.running = running;
    }

}
