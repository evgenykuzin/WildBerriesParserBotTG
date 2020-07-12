package commands;

import bot.Bot;

import java.util.ArrayList;

public class CommandManager {
    private ArrayList<Command> commands;
    private Buttons buttons;
    private Bot bot;
    public final static String HELP_TEXT = "Ты можешь отправить мне фото, видео или стикер и я сохраню это в своей базе." +
            " Любому человеку я могу рандомно отправить это фото/видео/стикер из своей базы по соответствующим командам.";

    public CommandManager(Bot bot) {
        this.bot = bot;
        commands = new ArrayList<>();
        buttons = new Buttons();
        buttons.createUserKeyboard(this);
        addCommand(new Command("Помощь", HELP_TEXT));

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
