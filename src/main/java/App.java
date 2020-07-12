import bot.Bot;
import bot.Sender;
import entities.Product;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class App {
    private static Queue<Product> productQueue;
    private static Set<String> linksSet;

    public static void main(String[] args) throws IOException {
        initCollections();
        ShopParser shopParser = new ShopParser();
        Bot bot = initBot();
        Sender sender = new Sender(bot, shopParser, linksSet);
        sender.interrupt();
        bot.setSender(sender);
        sender.start();
    }

    private static Bot initBot() {
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

    private static void initCollections() throws IOException {
        productQueue = new ConcurrentLinkedQueue<>();
        linksSet = new HashSet<>();
        String filePath = new File("").getAbsolutePath() + "/src/main/resources/files/categoryLinks.txt";
        linksSet.addAll(Files.readAllLines(new File(filePath).toPath()));

    }
}
