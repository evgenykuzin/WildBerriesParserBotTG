import bot.Bot;
import bot.Sender;
import database.DatabaseManager;
import entities.Product;
import exceptions.DBConnectionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.ShopParser;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class SenderTest {
    @Test
    public void testUpdate() throws IOException {
        Bot bot = new Bot(new DatabaseManager(), true);
        ShopParser shopParser = new ShopParser();
        Set<String> linksSet = new HashSet<>();
        Sender sender = new Sender(bot, shopParser, new DatabaseManager());
        String url = "https://www.wildberries.ru/catalog/7696800/detail.aspx?targetUrl=GP";
        String productName = "Бандана";
        String brandName = "COLORE CALDO /";

        Product expectedProduct = new Product(url);
        expectedProduct.setProductName(productName);
        expectedProduct.setBrandName(brandName);
        expectedProduct.setNewPrice(30.6);
        expectedProduct.setOldPrice(51.0);
        expectedProduct.setDiscountPercent(40.0);

        Product parsedProduct = new Product(url);
        parsedProduct.setProductName(productName);
        parsedProduct.setBrandName(brandName);
        parsedProduct.setNewPrice(30.6);
        parsedProduct.setOldPrice(510.0);
        parsedProduct.setDiscountPercent(94.0);

        double savedProductPrice = 51.0;
        sender.compareAndUpdateProducts(parsedProduct, savedProductPrice);
        double updatedProductPrice = -1;
        try {
            updatedProductPrice = new DatabaseManager().getExistingProductPriceByUrl(parsedProduct.getUrl());
        } catch (DBConnectionException throwables) {
            throwables.printStackTrace();
        }
        Assertions.assertEquals(expectedProduct.getNewPrice(), updatedProductPrice);
        try {
            new DatabaseManager().updateProduct(expectedProduct);
        } catch (DBConnectionException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void testMap() {
        Map<String, Double> map = new HashMap<>();
        map.put("a", 0.1);
        System.out.println(map.get("a"));
        System.out.println(map.get("b"));
        Double k = map.get("c");
        System.out.println(k);
        map.put("d", 10.0);
        map.replace("d", 15.0);
        System.out.println(map.get("d"));
    }

    @Test
    public void testQueue() {
        Queue<String> queue = new ArrayBlockingQueue<>(20);
        for (int i = 0; i < 30; i++) {
            queue.offer("string" + i);
        }
        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }
        System.out.println("queue = "+queue);
    }

}
