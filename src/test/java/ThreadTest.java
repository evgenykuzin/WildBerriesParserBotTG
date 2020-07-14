import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {


    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        SomeThread someThread = new SomeThread(i);
        ExecutorService executorService = Executors.newCachedThreadPool();
        InfinityThread infinityThread = new InfinityThread();
        infinityThread.start();
        start(someThread, i);
        DoctorThread doctorThread = new DoctorThread(someThread, i);
        doctorThread.start();
    }

    private static void start(SomeThread someThread, int i) {
        //        try {
            someThread.setI(i).start();

//        } catch (OutOfMemoryError e) {
//            i++;
//            try {
//                restart(someThread, i);
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }
//        }
    }

    private static void restart(SomeThread someThread, int i) throws InterruptedException {
        System.out.println("haha catch you!");
//            executorService.submit(someThread);
        someThread.interrupt();
        Thread.sleep(6000);
        i++;
        //executorService.execute(someThread.setI(i));
        start(someThread, i);
    }

    private static class DoctorThread extends Thread {
        SomeThread someThread;
        int i;

        public DoctorThread(SomeThread someThread, int i) {
            this.someThread = someThread;
            this.i = i;
        }

        @Override
        public void run() {
            while (true) {
                if (!someThread.isAlive()) {
                    System.out.println("healing");
                    //ThreadTest.start(someThread, i+1);
                    someThread.setI(i+1).start();
                }
            }
        }
    }

    private static class InfinityThread extends Thread {
        @Override
        public void run() {
            long time = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - time > 2000) {
                    time = System.currentTimeMillis();
                    System.out.println("background");
                }
            }
        }
    }

    private static class SomeThread extends Thread {
        int i;

        public SomeThread(int i) {
            setI(i);
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();

                while (true) {
                    if (System.currentTimeMillis() - time > 2000) {
                        time = System.currentTimeMillis();
                        System.out.println("working " + i);
                        if (new Random().nextInt(3) == 1) {
                            System.out.println("out of memory pacani");
                            throw new OutOfMemoryError();
                        }
                    }
                }

        }

        SomeThread setI(int i) {
            this.i = i;
            return this;
        }
    }
}
