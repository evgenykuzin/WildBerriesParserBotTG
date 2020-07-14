import com.mysql.cj.exceptions.ConnectionIsClosedException;
import org.junit.jupiter.api.Test;

public class ThreadTest {
    @Test
    public void testThrowsException() {
        SomeThread someThread = new SomeThread();
        try {
            someThread.run();
        } catch (OutOfMemoryError | ConnectionIsClosedException throwable) {
            throwable.printStackTrace();
            System.out.println("haha catch you!");
        }
    }

    private static class SomeThread extends Thread {
        @Override
        public void run() throws OutOfMemoryError, ConnectionIsClosedException {
            long time = System.currentTimeMillis();
            while (true) {
                System.out.println("working");
                if (System.currentTimeMillis() - time > 10000) {
throw new OutOfMemoryError("out of memory pacani");
                }
            }
        }
    }
}
