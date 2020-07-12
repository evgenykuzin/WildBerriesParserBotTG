package commands;

import bot.Bot;

import java.util.ArrayList;

public class CommandManager {
    private ArrayList<Command> commands;
    private Buttons buttons;
    private Bot bot;
    public final static String HELP_TEXT = "";

    public CommandManager(Bot bot) {
        this.bot = bot;
        commands = new ArrayList<>();
        buttons = new Buttons();
        buttons.createUserKeyboard(this);

        addCommand(new Command("/help", HELP_TEXT));

        Command seenCmd = new Command("/seen");
        seenCmd.setAction(message -> {
            if(message.hasText()) {
                String productId = message.getText().replace(seenCmd.getName(), "");
                //TODO
            }
            return null;
        });
        addCommand(seenCmd);

        Command catListCmd = new Command("/cat_list");
        catListCmd.setAction(message -> {
            //TODO
            return null;
        });
        addCommand(catListCmd);

        Command catAddCmd = new Command("/cat_add");
        catListCmd.setAction(message -> {
            String name = message.getText().replace(catAddCmd.getName(), "");

            //TODO
            return null;
        });
        addCommand(catAddCmd);

        Command catRmCmd = new Command("/cat_rm");
        catListCmd.setAction(message -> {
            String name = message.getText().replace(catRmCmd.getName(), "");

            //TODO
            return null;
        });
        addCommand(catAddCmd);

        Command igListcmd = new Command("/ig_list");
        catListCmd.setAction(message -> {
            //TODO
            return null;
        });
        addCommand(catListCmd);

        Command igAddCmd = new Command("/ig_add");
        catListCmd.setAction(message -> {
            String[] split = message.getText().replace(igAddCmd.getName(), "").split("=");
            String name = split[0];
            double price = 0.0;
            if (split.length > 1) price = Double.parseDouble(split[1]);

            //TODO
            return null;
        });
        addCommand(catAddCmd);

        Command igRmCmd = new Command("/ig_rm");
        catListCmd.setAction(message -> {
            String name = message.getText().replace(igRmCmd.getName(), "");

            //TODO
            return null;
        });
        addCommand(catAddCmd);

        Command preloadCmd = new Command("/preload");
        catListCmd.setAction(message -> {

            //TODO
            return null;
        });
        addCommand(catAddCmd);

        Command configCmd = new Command("/config");
        catListCmd.setAction(message -> {

            //TODO
            return null;
        });
        addCommand(catAddCmd);
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

}
