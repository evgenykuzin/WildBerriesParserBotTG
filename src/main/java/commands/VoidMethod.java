package commands;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

public class VoidMethod extends BotApiMethod {
    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String deserializeResponse(String s) throws TelegramApiRequestException {
        return null;
    }

    @Override
    public void validate() throws TelegramApiValidationException {

    }
}
