package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class GroupLeaderHelpHandler implements MessageHandler {

    private UserDataCache userDataCache;

    public GroupLeaderHelpHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.SHOW_GROUP_LEAD_PANEL);
        return new SendMessage(message.getChatId(), "Информация по пользованию панелью админа (лидера группы)");
    }

    @Override
    public BotState getHandleName() {
        return BotState.SEND_REPLY_ON_GROUP_HELP;
    }
}
