import context.Context;

public class App {
    public static void main(String[] args) throws InterruptedException {
        Context.setSender();
        Context.startSender();
    }
}
