import bot.Bot;
import bot.Sender;
import database.DatabaseManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;

import java.io.IOException;
import java.util.Set;

public class App {
    public static void main(String[] args) throws IOException {
        DatabaseManager databaseManager = new DatabaseManager();
        Set<String> linksSet = databaseManager.getAllCategories();
        ShopParser shopParser = new ShopParser();
        Bot bot = initBot(databaseManager);
        Sender sender = new Sender(bot, shopParser, linksSet, databaseManager);
        bot.setSender(sender);
        sender.start();
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
