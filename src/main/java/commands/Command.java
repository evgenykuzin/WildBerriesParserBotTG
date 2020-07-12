package commands;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


public class Command {
    private String name;
    private ActionSetter actionSetter;
    public Command(String name){
        this.name = name;
        actionSetter = new SendMessageActionSetter(name);
    }

    public Command(String name, ActionSetter actionSetter){
        this.name = name;
        this.actionSetter = actionSetter;
    }

    public Command(String name, String answer) {
        this.name = name;
        actionSetter = new SendMessageActionSetter(answer);

    }

    public boolean isName(String name){
        return name.contains(this.name);
    }

    public String getName() {
        return name;
    }

    public void setAction(ActionSetter actionSetter){
        this.actionSetter = actionSetter;
    }

    public BotApiMethod action(Message message){
        return actionSetter.action(message);
    }

    public static abstract class ActionSetter {
        public abstract BotApiMethod action(Message message);
    }

    public static class SendMessageActionSetter extends ActionSetter{
        private String msg;
        public SendMessageActionSetter(String msg){
            this.msg = msg;
        }
        public synchronized BotApiMethod sendMsg(Message message, String s) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText(s);
            return sendMessage;
        }
        @Override
        public BotApiMethod action(Message message) {
            return sendMsg(message, msg);
        }
    }

}
