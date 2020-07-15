package entities;

import java.util.Objects;

public class Product {
    String url, productName, brandName;
    double oldPrice, newPrice, discountPercent;

    public Product(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String constructMessage() {
        return "[" +
                brandName +
                " \\ " +
                productName +
                "]\nЦена: " +
                newPrice +
                " < " +
                oldPrice +
                " = " +
                discountPercent +
                "%\n" +
                url;
    }

    @Override
    public String toString() {
        return "Product{" +
                "url='" + url + '\'' +
                ", productName='" + productName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", newPrice=" + newPrice +
                ", oldPrice=" + oldPrice +
                ", discountPercent=" + discountPercent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.oldPrice, oldPrice) == 0 &&
                Double.compare(product.newPrice, newPrice) == 0 &&
                Double.compare(product.discountPercent, discountPercent) == 0 &&
                Objects.equals(url, product.url) &&
                Objects.equals(productName, product.productName) &&
                Objects.equals(brandName, product.brandName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, productName, brandName, oldPrice, newPrice, discountPercent);
    }
}
