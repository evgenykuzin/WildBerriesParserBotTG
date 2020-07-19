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
    private Sender sender;
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private static final long chatId = 443215848;
    public static final long testChatId = 328018558;

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
        commandManager = new CommandManager(this);
        System.out.println("run!");
    }

    private void loadProps() {
        Properties botProps = PropertiesManager.getProperties("bot");
        botName = botProps.getProperty("bot.name");
        botToken = botProps.getProperty("bot.token");
        //chatId = Long.parseLong(botProps.getProperty("bot.chat_id")); //вернуть назад!
    }

    private void onDocument(Message message) {
        if (message.hasDocument()) {
            try {
                execute(commandManager.onDocument(message));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void onText(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            System.out.println("chatId = " + message.getChatId());
            System.out.println(text);
            if (text.equals("/start")) {
                sendTextToUser("start parsing...", message.getChatId());
                sender = Context.restartSender();
                sender.setRunning(Boolean.TRUE);
            } else if (text.equals("/stop")) {
                sendTextToUser("stopping...", message.getChatId());
                sender.setRunning(Boolean.FALSE);
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
        onDocument(message);
    }

    public synchronized void sendTextToUser(String text, long chatId) {
        SendMessage sendMessage = constructSendMessage(text, chatId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendText(String text) {
        sendTextToUser(text, chatId);
        sendTextToUser(text, testChatId);
    }

    public SendMessage constructSendMessage(String text, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    public long getChatId() {
        return chatId;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
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
