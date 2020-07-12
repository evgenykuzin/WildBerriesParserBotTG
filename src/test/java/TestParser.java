import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestParser {

    @Test
    public void testRegex(){
        String string = "a,b c d,eg";
        String[] split = string.split("[\\s,]");
        System.out.println(Arrays.toString(split));
    }
}
