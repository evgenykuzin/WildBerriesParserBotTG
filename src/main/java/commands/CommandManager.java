package commands;

import bot.Bot;
import database.DatabaseManager;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class CommandManager {
    private ArrayList<Command> commands;
    private Buttons buttons;
    private Bot bot;
    private DatabaseManager databaseManager;
    public final static String HELP_TEXT = "i am not useless!";

    public CommandManager(Bot bot, DatabaseManager databaseManager) {
        this.bot = bot;
        this.databaseManager = databaseManager;
        commands = new ArrayList<>();
        buttons = new Buttons();
        buttons.createUserKeyboard(this);

        addCommand(new Command("help", HELP_TEXT));

        Command seenCmd = new Command("seen");
        seenCmd.setAction(message -> {
            if (message.hasText()) {
                String productUrl = message.getText().replace(seenCmd.getName(), "");
                //TODO
            }
            return sendMessage("uhh...don't know...", message.getChatId());
        });
        addCommand(seenCmd);

        Command catListCmd = new Command("cat_list");
        catListCmd.setAction(message -> {
            Set<String> categories = databaseManager.getAllCategories();
            if (categories == null || categories.isEmpty()) {
                return sendMessage("categories is empty(", message.getChatId());
            }
            try {
                bot.execute(sendDocument(categories, "categories", message.getChatId()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return new VoidMethod();
        });
        addCommand(catListCmd);

        Command catAddCmd = new Command("cat_add");
        catAddCmd.setAction(message -> {
            String[] categories = getLinesWithoutCommand(message.getText(), catAddCmd.getName());
            if (categories.length == 0) {
                return sendMessage("please, enter a urls of categories you need. Split by spaces.", message.getChatId());
            }
            Set<String> existing = databaseManager.getAllCategories();
            for (String url : categories) {
                if (!url.contains("http")) continue;
                if(existing.contains(url)) continue;
                databaseManager.saveCategory(url);
            }
            return sendMessage("categories saved!", message.getChatId());
        });
        addCommand(catAddCmd);

        Command catRmCmd = new Command("cat_rm");
        catRmCmd.setAction(message -> {
            String[] categories = getLinesWithoutCommand(message.getText(), catRmCmd.getName());
            if (categories.length == 0)
                return sendMessage("please, enter a urls of categories you need. Split by spaces.", message.getChatId());
            for (String url : categories) {
                databaseManager.removeCategory(url);
            }
            return sendMessage("categories removed!", message.getChatId());
        });
        addCommand(catRmCmd);

        Command igListCmd = new Command("ig_list");
        igListCmd.setAction(message -> {
            Set<String> ignoredBrands = databaseManager.getAllIgnoredBrands();
            if (ignoredBrands == null || ignoredBrands.isEmpty()) {
                return sendMessage("ignored brands list is empty(", message.getChatId());
            }
            StringBuilder sb = new StringBuilder();
            ignoredBrands.forEach(c -> sb.append(c).append("\n"));
            return sendMessage(sb.toString(), message.getChatId());
        });
        addCommand(igListCmd);

        Command igAddCmd = new Command("ig_add");
        igAddCmd.setAction(message -> {
            String[] ignoredBrands = getLinesWithoutCommand(message.getText(), igAddCmd.getName());
            if (ignoredBrands.length == 0) return sendMessage("please, enter a brand names you need. Split by spaces.", message.getChatId());
            Set<String> existing = databaseManager.getAllIgnoredBrands();
            for (String brand : ignoredBrands) {
                if (brand.isEmpty() || brand.matches("[\\s\n,]")) continue;
                if(existing.contains(brand)) continue;
                databaseManager.saveIgnoredBrand(brand);
            }
            return sendMessage("brands ignored!", message.getChatId());
        });
        addCommand(igAddCmd);

        Command igRmCmd = new Command("ig_rm");
        igRmCmd.setAction(message -> {
            String[] ignoredBrands = getLinesWithoutCommand(message.getText(), igRmCmd.getName());
            if (ignoredBrands.length == 0) return sendMessage("please, enter a brand names you need. Split by spaces.", message.getChatId());
            for (String brand : ignoredBrands) {
                databaseManager.removeIgnoredBrand(brand);
            }
            return sendMessage("brands not ignored anymore!", message.getChatId());
        });
        addCommand(igRmCmd);

        Command preloadCmd = new Command("preload");
        preloadCmd.setAction(message -> {

            //TODO
            return sendMessage("mmm...what??", message.getChatId());
        });
        addCommand(preloadCmd);

        Command configCmd = new Command("config");
        configCmd.setAction(message -> {

            //TODO
            return sendMessage("aaa... config?", message.getChatId()); //заменить на bot.getChatId;
        });
        addCommand(configCmd);
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public boolean contains(String name) {
        for (Command command : commands) {
            if (command.getName().equals(name)) return true;
        }
        return false;
    }

    public Command getCommand(String name) {
        for (Command command : commands) {
            if (command.getName().equals(name)) {
                return command;
            }
        }
        return null;
    }

    private SendMessage sendMessage(String text, long chatId) {
        return bot.constructSendMessage(chatId, text); //заменить на bot.getChatId()
    }

    private SendDocument sendDocument(Iterable<? extends CharSequence> content, String fileName, long chatId) {
        File file = null;
        try {
            file = constructPath(content, fileName).toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(file);
        return sendDocument;
    }

    public Path constructPath(Iterable<? extends CharSequence> content, String fileName) throws IOException{
        Path path = Files.createTempFile(fileName, ".txt");
        Files.write(path, content);
        return path;
    }

    private String[] getLinesWithoutCommand(String string, String command) {
        return string.replace("/"+command, "").replaceAll("[\\s\n,]", ",").split(",");
    }
}
