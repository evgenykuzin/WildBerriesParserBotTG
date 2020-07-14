package context;

import bot.Bot;
import bot.Sender;
import database.DatabaseManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;

import java.util.Set;

public class Context {
    public static DatabaseManager databaseManager = databaseManager();
    public static Set<String> linksSet = linksSet(databaseManager);
    public static ShopParser shopParser = shopParser();
    public static Bot bot = bot(databaseManager);
    public static Sender sender = sender(bot, shopParser, linksSet, databaseManager);

    private static DatabaseManager databaseManager() {
        return new DatabaseManager();
    }

    private static Set<String> linksSet(DatabaseManager databaseManager) {
        return databaseManager.getAllCategories();
    }

    private static ShopParser shopParser() {
        return new ShopParser();
    }

    private static Sender sender(Bot bot, ShopParser shopParser, Set<String> linksSet, DatabaseManager databaseManager) {
        return new Sender(bot, shopParser, linksSet, databaseManager);
    }

    public static void startSender() throws InterruptedException {
        try {
            sender.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            restartSender(60000);
        }
    }

    public static void restartSender(long time) {
        try {
            sender.interrupt();
            System.out.println("restarting thread Sender");
            Thread.sleep(time);
            startSender();
            System.out.println("thread Sender restarted");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static Bot bot(DatabaseManager databaseManager) {
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

    public static void initBot() {
        bot.setSender(sender);
    }
}
