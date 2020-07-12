package commands;

import bot.Bot;
import database.DatabaseManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

public class CommandManager {
    private ArrayList<Command> commands;
    private Buttons buttons;
    private Bot bot;
    private DatabaseManager databaseManager;
    public final static String HELP_TEXT = "i am not useless!";

    public CommandManager(Bot bot) {
        this.bot = bot;
        databaseManager = bot.getDatabaseManager();
        commands = new ArrayList<>();
        buttons = new Buttons();
        buttons.createUserKeyboard(this);

        addCommand(new Command("help", HELP_TEXT));

        addCommand(new Command("start", message -> {
            bot.getSender().setRunning(Boolean.TRUE);
            return sendMessage("start parsing...");
        }));

        addCommand(new Command("stop", message -> {
            bot.getSender().setRunning(Boolean.FALSE);
            return sendMessage("stopping...");
        }));

        Command seenCmd = new Command("/seen");
        seenCmd.setAction(message -> {
            if (message.hasText()) {
                String productUrl = message.getText().replace(seenCmd.getName(), "");
                //TODO
            }
            return sendMessage("uhh...don't know...");
        });
        addCommand(seenCmd);

        Command catListCmd = new Command("/cat_list");
        catListCmd.setAction(message -> {
            Set<String> categories = databaseManager.getAllCategories();
            if (categories == null || categories.isEmpty()) {
                return sendMessage("categories is empty(");
            }
            StringBuilder sb = new StringBuilder();
            categories.forEach(c -> sb.append(c).append("\n"));
            return sendMessage(sb.toString());
        });
        addCommand(catListCmd);

        Command catAddCmd = new Command("/cat_add");
        catAddCmd.setAction(message -> {
            String[] categories = message.getText().replace(catAddCmd.getName(), "").split(" ");
            if (categories.length == 0)
                return sendMessage("please, enter a urls of categories you need. Split by spaces.");
            for (String url : categories) {
                databaseManager.saveCategory(url);
            }
            return sendMessage("categories saved!");
        });
        addCommand(catAddCmd);

        Command catRmCmd = new Command("/cat_rm");
        catRmCmd.setAction(message -> {
            String[] categories = message.getText().replace(catRmCmd.getName(), "").split(" ");
            if (categories.length == 0)
                return sendMessage("please, enter a urls of categories you need. Split by spaces.");
            for (String url : categories) {
                databaseManager.removeCategory(url);
            }
            return sendMessage("categories removed!");
        });
        addCommand(catRmCmd);

        Command igListCmd = new Command("/ig_list");
        igListCmd.setAction(message -> {
            Set<String> ignoredBrands = databaseManager.getAllIgnoredBrands();
            if (ignoredBrands == null || ignoredBrands.isEmpty()) {
                return sendMessage("ignored brands list is empty(");
            }
            StringBuilder sb = new StringBuilder();
            ignoredBrands.forEach(c -> sb.append(c).append("\n"));
            return sendMessage(sb.toString());
        });
        addCommand(igListCmd);

        Command igAddCmd = new Command("/ig_add");
        igAddCmd.setAction(message -> {
            String[] split = message.getText().replace(igAddCmd.getName(), "").split("=");
            String[] ignoredBrands = split[0].split("[\\s,]");
            double price = 0.0;
            if (split.length > 1) price = Double.parseDouble(split[1]);
            if (ignoredBrands.length == 0) return sendMessage("please, enter a brand names you need. Split by spaces.");
            for (String brand : ignoredBrands) {
                databaseManager.saveIgnoredBrand(brand);
            }
            return sendMessage("brands ignored!");
        });
        addCommand(igAddCmd);

        Command igRmCmd = new Command("/ig_rm");
        igRmCmd.setAction(message -> {
            String[] ignoredBrands = message.getText().replace(igRmCmd.getName(), "").split(" ");
            if (ignoredBrands.length == 0) return sendMessage("please, enter a brand names you need. Split by spaces.");
            for (String brand : ignoredBrands) {
                databaseManager.removeIgnoredBrand(brand);
            }
            return sendMessage("brands not ignored anymore!");
        });
        addCommand(igRmCmd);

        Command preloadCmd = new Command("/preload");
        preloadCmd.setAction(message -> {

            //TODO
            return sendMessage("mmm...what??");
        });
        addCommand(preloadCmd);

        Command configCmd = new Command("/config");
        configCmd.setAction(message -> {

            //TODO
            return sendMessage("aaa... config?"); //заменить на bot.getChatId;
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

    private SendMessage sendMessage(String text) {
        return bot.constructSendMessage(bot.testChatId, text); //заменить на bot.getChatId()
    }

}
