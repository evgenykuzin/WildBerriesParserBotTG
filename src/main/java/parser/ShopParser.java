package parser;

import com.google.common.base.CharMatcher;
import context.Context;
import entities.Product;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class ShopParser {
    Set<String> linksSet;

    public ShopParser(){}

    public Elements parseCategory(String link) {
        Document doc;
        try {
            doc = Jsoup.parse(new URL(link), 0);
            return doc.getElementsByClass("dtList-inner");
        } catch (HttpStatusException hse) {
            System.out.println("error parsing url: " + hse.getUrl());
            if (link.contains(".ru")) {
                System.out.println("trying '.kz' ...");
                return parseCategory(link.replace(".ru", ".kz"));
            } else {
                System.out.println("failed to parse url with '.kz'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Elements();
    }

    public Product parseProduct(Element element, Set<String> ignoredBrands) {
        Product product;
        String productName = element.getElementsByClass("goods-name c-text-sm").text();
        String brandName = element.getElementsByClass("brand-name c-text-sm").text();
        if (ignoredBrands.contains(brandName)) return null;
        String np = element.getElementsByClass("lower-price").text();
        String dp = element.getElementsByClass("price-sale active").text();
        String op = element.getElementsByClass("price-old-block").html().split("</del>")[0];
        double newPrice = getDouble(np);
        double discountPercent = getDouble(dp);
        double oldPrice = getDouble(op);
        if (newPrice == -1 || oldPrice == -1 || discountPercent == -1) return null;
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

    public Set<String> getLinksSet() {
        return linksSet;
    }

    public void setLinksSet(Set<String> linksSet) {
        this.linksSet = linksSet;
    }

}
