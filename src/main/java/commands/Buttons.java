package commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Buttons {
    private ArrayList<KeyboardRow> rows;
    private ArrayList<String> buttons;
    private ReplyKeyboardMarkup replyKeyboardMarkup;
    private InlineKeyboardMarkup inlineKeyboardMarkup;

    public Buttons() {
        rows = new ArrayList<>();
        buttons = new ArrayList<>();
    }

    public void createUserKeyboard(CommandManager commandManager) {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        KeyboardRow start = new KeyboardRow();
        start.add(new KeyboardButton("start"));
        rows.add(start);
        rows.add(new KeyboardRow());
        addReplyButton("random_sticker", 2);
        addReplyButton("random_meme", 2);
        addReplyButton("random_video", 2);
        int level = 1;
        for (Command command : commandManager.getCommands()) {
            if (!buttons.contains(command.getName())) {
                addReplyButton(command.getName(), level);
                level++;
            }
        }
        addReplyButton("mark", level + 1);
    }

    public void createMarksKeyboard() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(new ArrayList<>());
        addInlineButton("лайк", rows);
        //addInlineButton("дизлайк", rows);
    }

    public void addReplyButton(String name, int level) {
        KeyboardRow row;
        if (rows.isEmpty()) {
            row = new KeyboardRow();
        } else {
            row = rows.get(rows.size() - 1);
        }
        if (!rows.isEmpty() && level != -1) {
            while (rows.size() <= level) {
                rows.add(new KeyboardRow());
            }
            row = rows.get(level);
        }
        KeyboardButton button = new KeyboardButton(name);
        row.add(button);
        if (!rows.contains(row)) {
            rows.add(row);
        }
        replyKeyboardMarkup.setKeyboard(rows);
        buttons.add(button.getText());
    }

    public void addInlineButton(String name, List<List<InlineKeyboardButton>> inlineRows){
        InlineKeyboardButton button = new InlineKeyboardButton(name);
        inlineRows.get(0).add(button);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
    }

    public ReplyKeyboardMarkup getReplyKeyboard() {
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboard() {
        return inlineKeyboardMarkup;
    }
}
