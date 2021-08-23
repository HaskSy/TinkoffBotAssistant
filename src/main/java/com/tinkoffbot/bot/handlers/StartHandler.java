package com.tinkoffbot.bot.handlers;

import com.tinkoffbot.bot.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartHandler implements MessageHandler {

    @Override
    public SendMessage handle(Message message) {
        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.START;
    }

}
