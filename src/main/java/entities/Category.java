package entities;

import org.jsoup.select.Elements;

public class Category extends Elements {
    public static Elements set(Elements elements) {
        return elements;
    }
    public static Category emptyCategory (){
        return new Category();
    }
}
