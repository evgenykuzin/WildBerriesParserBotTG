package bot;

import commands.Command;
import commands.CommandManager;
import context.Context;
import database.DatabaseManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import properties.PropertiesManager;

import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    private String botName;
    private String botToken;
    private long chatId = 443215848;
    private Sender sender;
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    public static final long testChatId = 328018558; //убрать!

    public Bot() {
        this(Context.databaseManager);
    }

    public Bot(DatabaseManager databaseManager) {
        this(databaseManager, false);
    }

    public Bot(DatabaseManager databaseManager, boolean withoutProps) {
        if (!withoutProps) {
            loadProps();
        }
        this.databaseManager = databaseManager;
        commandManager = new CommandManager(this, databaseManager);
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
            if (text.equals("/start")) {
                sendText("start parsing...");
                sender = Context.restartSender(0);
                sender.setRunning(Boolean.TRUE);
            } else if (text.equals("/stop")) {
                sendText("stopping...");
                sender.setRunning(Boolean.FALSE);
                sender.interrupt();
            }
            for (Command command : commandManager.getCommands()) {
                if (text.contains("/" + command.getName())) {
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
        System.out.println("chatId = " + update.getMessage().getChatId());
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

    public long getChatId() {
        return chatId;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
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
