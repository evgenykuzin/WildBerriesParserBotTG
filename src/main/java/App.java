import context.Context;
import org.tanukisoftware.wrapper.security.*;
import org.tanukisoftware.wrapper.WrapperSimpleApp;
import org.tanukisoftware.wrapper.WrapperStartStopApp;

public class App {
    public static void main(String[] args) {
        WrapperSimpleApp wrapperSimpleApp;
        Context.setSender();
        Context.restartSender();
    }
}
