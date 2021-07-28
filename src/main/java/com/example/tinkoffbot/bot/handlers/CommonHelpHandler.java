package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class CommonHelpHandler implements MessageHandler {

    private UserDataCache userDataCache;

    public CommonHelpHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.SHOW_MAIN_MENU);
        return new SendMessage(message.getChatId(), "Подсказки по пользованию ботом");
    }

    @Override
    public BotState getHandleName() {
        return BotState.SEND_REPLY_ON_HELP;
    }
}
