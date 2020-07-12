package bot;

import commands.Command;
import commands.CommandManager;
import database.DatabaseManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;
import properties.PropertiesManager;

import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    private String botName;
    private String botToken;
    private long chatId = 443215848;
    private ShopParser shopParser;
    private Sender sender;
    private CommandManager commandManager;
    private DatabaseManager databaseManager;
    public static final long testChatId = 328018558; //убрать!

    public Bot() {
        this(false);
    }

    public Bot(boolean withoutProps) {
        if (!withoutProps) {
            loadProps();
        }
        commandManager = new CommandManager(this);
        System.out.println("run!");
    }

    private void loadProps() {
        Properties botProps = PropertiesManager.getProperties("testbot"); //заменить на bot!
        botName = botProps.getProperty("bot.name");
        botToken = botProps.getProperty("bot.token");
        //chatId = Long.parseLong(botProps.getProperty("bot.chat_id")); //вернуть назад!
    }

    private void onText(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            for (Command command : commandManager.getCommands()) {
                if (text.contains("/"+command.getName())) {
                    try {
                        execute(command.action(message));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        System.out.println("chatId = " + chatId);
        System.out.println(message.getText());
        onText(message);
    }

    public synchronized void sendTextToUser(long chatId, String s) {
        SendMessage sendMessage = constructSendMessage(chatId, s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendPhotoToUser(long chatId, String photo) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(photo);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendText(String text) {
        sendTextToUser(chatId, text);
        sendTextToUser(testChatId, text);
    }

    public SendMessage constructSendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    public ShopParser getShopParser() {
        return shopParser;
    }

    public void setShopParser(ShopParser shopParser) {
        this.shopParser = shopParser;
    }

    public long getChatId() {
        return chatId;
    }

    public Sender getSender() {
        return sender;
    }

    public void init(Sender sender) {
        this.sender = sender;
        sender.setRunning(Boolean.TRUE);
        databaseManager = sender.getDatabaseManager();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

}
