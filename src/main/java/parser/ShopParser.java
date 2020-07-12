package parser;

import bot.Bot;
import com.google.common.base.CharMatcher;
import entities.Category;
import entities.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.BlackLists;

import java.io.IOException;
import java.net.URL;
import java.util.Queue;
import java.util.Set;

public class ShopParser {
    Set<String> linksSet;
    Queue<Product> productQueue;
    Bot bot;

    public ShopParser(Set<String> linksSet, Queue<Product> productQueue) {
        this.linksSet = linksSet;
        this.productQueue = productQueue;
    }

    public ShopParser(){}

    public void parse() {
        for (String link : linksSet) {
            Elements category = parseCategory(link);
            for (Element element : category) {
                Product product = parseProduct(element);
                productQueue.add(product);
            }
        }
    }

    public Elements parseCategory(String link) {
        Document doc;
        try {
            doc = Jsoup.parse(new URL(link), 1000);
            return doc.getElementsByClass("dtList-inner");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Elements();
    }

    public Product parseProduct(Element element) {
        Product product;
        String productName = element.getElementsByClass("goods-name c-text-sm").text();
        String brandName = element.getElementsByClass("brand-name c-text-sm").text();
        if (BlackLists.containsInBrandBL(brandName)) return null;
        String np = element.getElementsByClass("lower-price").text();
        String dp = element.getElementsByClass("price-sale active").text();
        String op = element.getElementsByClass("price-old-block").html().split("</del>")[0];
        double newPrice = getDouble(np);
        double discountPercent = getDouble(dp);
        double oldPrice = getDouble(op);
        if (newPrice == -1 || oldPrice == -1 || discountPercent == -1) return null;
        //if (discountPercent < 40.0 && newPrice >= 100) return null;
        String url = element.getElementsByClass("ref_goods_n_p j-open-full-product-card").attr("href");
        product = new Product(url);
        product.setProductName(productName);
        product.setBrandName(brandName);
        product.setNewPrice(newPrice);
        product.setOldPrice(oldPrice);
        product.setDiscountPercent(discountPercent);
        return product;
    }

    private double getDouble(String string) {
        try {
            return Double.parseDouble(CharMatcher.digit().retainFrom(string));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public Set<String> getLinksSet() {
        return linksSet;
    }

    public void setLinksSet(Set<String> linksSet) {
        this.linksSet = linksSet;
    }

    public Queue<Product> getProductQueue() {
        return productQueue;
    }

    public void setProductQueue(Queue<Product> productQueue) {
        this.productQueue = productQueue;
    }

    private void sendMessage(String message) {
        bot.sendText(message);
    }
}