import entities.Product;
import org.junit.jupiter.api.Test;
import parser.ShopParser;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TestParser {

    @Test
    public void test() {
        Set<String> linksSet = new HashSet<>();
        String[] links = new String[] {};

        Queue<Product> productQueue = new ConcurrentLinkedDeque<>();
        ShopParser shopParser = new ShopParser(linksSet, productQueue);
        shopParser.parse();
    }
}
