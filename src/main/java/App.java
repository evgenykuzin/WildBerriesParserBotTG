import context.Context;
import org.tanukisoftware.wrapper.WrapperSimpleApp;

public class App {
    public static void main(String[] args) {
        WrapperSimpleApp wrapperSimpleApp;
        Context.setSender();
        Context.restartSender();
    }
}
