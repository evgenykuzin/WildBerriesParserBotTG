package commands;

import bot.Bot;
import context.Context;
import database.DatabaseManager;
import exceptions.DBConnectionException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            Set<String> categories = Context.sender.getCategories();
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
            if (databaseManager.isWaiting()) {
                return sendMessage("I'm tired! wait about 15 minutes and try again", message.getChatId());
            }
            String[] categories = getLinesWithoutCommand(message.getText(), catAddCmd.getName());
            if (categories.length == 0) {
                return sendMessage("please, enter a urls of categories you need. Split by spaces.", message.getChatId());
            }
            Set<String> existing = Context.sender.getCategories();
            for (String url : categories) {
                if (!url.contains("http")) {
                     bot.sendText(url + " is not valid url");
                     continue;
                }
                if (existing.contains(url)) {
                    bot.sendText(url + " already exists");
                    continue;
                }
                bot.sendText("saving " + url);
                try {
                    databaseManager.saveCategory(url);
                } catch (DBConnectionException e) {
                    e.printStackTrace();
                }
                Context.sender.addCategory(url);
                if (databaseManager.isWaiting()) {
                    return sendMessage("something wrong...", message.getChatId());
                }
            }
            return sendMessage("categories saved!", message.getChatId());
        });
        addCommand(catAddCmd);

        Command catRmCmd = new Command("cat_rm");
        catRmCmd.setAction(message -> {
            if (databaseManager.isWaiting()) {
                return sendMessage("i'm tired! wait about 15 minutes and try again", message.getChatId());
            }
            String[] categories = getLinesWithoutCommand(message.getText(), catRmCmd.getName());
            if (categories.length == 0)
                return sendMessage("please, enter a urls of categories you need. Split by spaces.", message.getChatId());
            for (String url : categories) {
                bot.sendText("removing " + url);
                try {
                    databaseManager.removeCategory(url);
                } catch (DBConnectionException e) {
                    e.printStackTrace();
                }
                if (databaseManager.isWaiting()) {
                    return sendMessage("something wrong...", message.getChatId());
                }
            }
            return sendMessage("categories removed!", message.getChatId());
        });
        addCommand(catRmCmd);

        Command catChCmd = new Command("cat_ch");
        catRmCmd.setAction(message -> {
            if (!message.hasDocument()) return sendMessage("Failed( You need to upload a file!", message.getChatId());
            List<String> categories = getLinesFromDocument(message.getDocument());
            if (categories.isEmpty()) {
                return sendMessage("please, write a urls of categories you need in the file", message.getChatId());
            }
            for (String url : categories) {
                if (!Context.sender.getCategories().contains(url)) {
                    try {
                        databaseManager.saveCategory(url);
                        Context.sender.addCategory(url);
                    } catch (DBConnectionException e) {
                        bot.sendText("failed to save " + url);
                        e.printStackTrace();
                    }
                }
            }
            return sendMessage("categories list changed!", message.getChatId());
        });
        addCommand(catChCmd);

        Command igListCmd = new Command("ig_list");
        igListCmd.setAction(message -> {
            Set<String> ignoredBrands = Context.sender.getIgnoredBrands();
            if (ignoredBrands == null || ignoredBrands.isEmpty()) {
                return sendMessage("ignored brands list is empty(", message.getChatId());
            }
            try {
                bot.execute(sendDocument(ignoredBrands, "ignored-brands", message.getChatId()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return new VoidMethod();
        });
        addCommand(igListCmd);

        Command igAddCmd = new Command("ig_add");
        igAddCmd.setAction(message -> {
            if (databaseManager.isWaiting()) {
                return sendMessage("i'm tired! wait about 15 minutes and try again", message.getChatId());
            }
            String[] ignoredBrands = getLinesWithoutCommand(message.getText(), igAddCmd.getName(), "[\n,]");
            if (ignoredBrands.length == 0)
                return sendMessage("please, enter a brand names you need. Split by spaces.", message.getChatId());
            Set<String> existing = Context.sender.getIgnoredBrands();
            for (String brand : ignoredBrands) {
                if (brand.isEmpty() || brand.matches("[\\s\n,]")) continue;
                if (existing.contains(brand)) {
                    bot.sendText(brand + "already ignored");
                    continue;
                }
                bot.sendText("saving " + brand + " to ignore list");
                try {
                    databaseManager.saveIgnoredBrand(brand);
                } catch (DBConnectionException e) {
                    e.printStackTrace();
                }
                Context.sender.addIgnoredBrand(brand);
                if (databaseManager.isWaiting()) {
                    return sendMessage("something wrong...", message.getChatId());
                }
            }
            return sendMessage("brands ignored!", message.getChatId());
        });
        addCommand(igAddCmd);

        Command igRmCmd = new Command("ig_rm");
        igRmCmd.setAction(message -> {
            if (databaseManager.isWaiting()) {
                return sendMessage("i'm tired! wait about 15 minutes and try again", message.getChatId());
            }
            String[] ignoredBrands = getLinesWithoutCommand(message.getText(), igRmCmd.getName(), "[\n,]");
            if (ignoredBrands.length == 0)
                return sendMessage("please, enter a brand names you need. Split by spaces.", message.getChatId());
            for (String brand : ignoredBrands) {
                bot.sendText("removing " + brand + " from ignore list");
                try {
                    databaseManager.removeIgnoredBrand(brand);
                } catch (DBConnectionException e) {
                    e.printStackTrace();
                }
                Context.sender.removeIgnoredBrand(brand);
                if (databaseManager.isWaiting()) {
                    return sendMessage("something wrong...", message.getChatId());
                }
            }
            return sendMessage("brands not ignored anymore!", message.getChatId());
        });
        addCommand(igRmCmd);

        Command igChCmd = new Command("ig_ch");
        catRmCmd.setAction(message -> {
            if (!message.hasDocument()) return sendMessage("Failed( You need to upload a file!", message.getChatId());
            List<String> ignoredBrands = getLinesFromDocument(message.getDocument());
            if (ignoredBrands.isEmpty()) {
                return sendMessage("please, write the names of brands, you want to ignore, in the file", message.getChatId());
            }
            for (String brand : ignoredBrands) {
                if (!Context.sender.getIgnoredBrands().contains(brand)) {
                    try {
                        databaseManager.saveIgnoredBrand(brand);
                        Context.sender.addIgnoredBrand(brand);
                    } catch (DBConnectionException e) {
                        bot.sendText("failed to save " + brand);
                        e.printStackTrace();
                    }
                }
            }
            return sendMessage("ignored brands list changed!", message.getChatId());
        });
        addCommand(igChCmd);

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

    public Path constructPath(Iterable<? extends CharSequence> content, String fileName) throws IOException {
        Path path = Files.createTempFile(fileName, ".txt");
        Files.write(path, content);
        return path;
    }

    private String[] getLinesWithoutCommand(String string, String command) {
        return getLinesWithoutCommand(string, command, "[\\s\n,]");
    }

    private String[] getLinesWithoutCommand(String string, String command, String delimiters) {
        return string.replace("/" + command + " ", "").replaceAll(delimiters, ",").split(",");
    }

    private List<String> getLinesFromDocument(Document document) {
        File file = null;
        try {
            file = loadFileFromInternet(document.getFileName().replace(".txt", ""),
                    document.getFileId(), bot.getBotToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file != null) {
            Path path = file.toPath();
            try {
                return Files.readAllLines(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bot.sendText("error uploading file!");
        return Collections.emptyList();
    }

    public static File loadFileFromInternet(String file_name, String file_id, String token) throws IOException {
        URL url = new URL("https://api.telegram.org/bot"+token+"/getFile?file_id="+file_id);
        BufferedReader in = new BufferedReader(new InputStreamReader( url.openStream()));
        String res = in.readLine();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");
        URL downoload = new URL("https://api.telegram.org/file/bot" + token + "/" + file_path);
        File file = Files.createTempFile(file_name, ".txt").toFile();
        FileOutputStream fos = new FileOutputStream(file);
        System.out.println("Start upload");
        ReadableByteChannel rbc = Channels.newChannel(downoload.openStream());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        System.out.println("Uploaded!");
        return file;
    }
}
