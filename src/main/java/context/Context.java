package context;

import bot.Bot;
import bot.Sender;
import database.DatabaseManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;

public class Context {
    public static DatabaseManager databaseManager = databaseManager();
    public static ShopParser shopParser = shopParser();
    public static Bot bot = bot();
    public static Sender sender = sender(bot, shopParser, databaseManager);

    private static DatabaseManager databaseManager() {
        return new DatabaseManager();
    }

    private static ShopParser shopParser() {
        return new ShopParser();
    }

    private static Sender sender(Bot bot, ShopParser shopParser, DatabaseManager databaseManager) {
        return new Sender(bot, shopParser, databaseManager);
    }

    public static Sender restartSender() {
        System.out.println("restarting thread Sender");
        try {
            sender = sender(bot, shopParser, databaseManager);
            sender.run();
            sender = restartSender();
            System.out.println("thread Sender restarted");
        } catch (OutOfMemoryError throwable) {
            System.out.println("out of memory");
            sender = restartSender();
            throwable.printStackTrace();
        }
        return sender;
    }

    private static Bot bot() {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        Bot bot = new Bot();
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException tae) {
            tae.printStackTrace();
        }
        return bot;
    }

    public static void setSender() {
        bot.setSender(sender);
    }
}
