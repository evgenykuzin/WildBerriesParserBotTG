import bot.Bot;
import bot.Sender;
import com.mysql.cj.exceptions.ConnectionIsClosedException;
import database.DatabaseManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;

import java.net.Socket;
import java.sql.SQLSyntaxErrorException;
import java.util.Set;

public class App {
    public static void main(String[] args) throws InterruptedException {
        DatabaseManager databaseManager = new DatabaseManager();
        Set<String> linksSet = databaseManager.getAllCategories();
        ShopParser shopParser = new ShopParser();
        Bot bot = initBot(databaseManager);
        Sender sender = new Sender(bot, shopParser, linksSet, databaseManager);
        bot.setSender(sender);
        startSender(sender);
    }

    private static void startSender(Sender sender) throws InterruptedException {
        try {
            sender.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println("restarting thread Sender");
            sender.interrupt();
            Thread.sleep(60000);
            startSender(sender);
            System.out.println("thread Sender restarted");
        }
    }

    private static Bot initBot(DatabaseManager databaseManager) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Bot bot = new Bot(databaseManager);
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException tae) {
            tae.printStackTrace();
        }
        return bot;
    }

}
