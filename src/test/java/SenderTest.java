import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class SenderTest {


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
