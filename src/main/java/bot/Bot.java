package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import parser.ShopParser;
import properties.PropertiesManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    private String botName;
    private String botToken;
    private long chatId;
    private ShopParser shopParser;
    private Sender sender;

    public Bot() {
        this(false);
    }

    public Bot(boolean withoutProps) {
        if (!withoutProps) {
            loadProps();
        }
    }

    private void loadProps() {
        Properties botProps = PropertiesManager.getProperties("testbot");
        botName = botProps.getProperty("bot.name");
        botToken = botProps.getProperty("bot.token");
        chatId = Long.parseLong(botProps.getProperty("bot.chat_id"));
    }

    private void onText(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals("/start")) {
                sendText("wait a few seconds...");
                sender.setRunning(Boolean.TRUE);
            } else if (text.equals("/stop")) {
                sendText("stopping...");
                sender.setRunning(Boolean.FALSE);
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        chatId = message.getChatId();
        System.out.println("chatId = " + chatId);;
        System.out.println(message.getText());
        onText(message);
    }

    public synchronized void sendTextToUser(long chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
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
    }

    public synchronized void sendPhoto(String photo) {
        sendPhotoToUser(chatId, photo);
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
