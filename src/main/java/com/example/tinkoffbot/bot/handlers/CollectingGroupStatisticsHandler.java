package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class CollectingGroupStatisticsHandler implements MessageHandler {

    private UserDataCache userDataCache;

    public CollectingGroupStatisticsHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.SHOW_KEYBOARD_GROUP_LEAD);
        return new SendMessage(message.getChatId(), "Здесь будет собираться статистика по всей группе (ссылка на собранный документ, расписанная или что-то такое)");
    }

    @Override
    public BotState getHandleName() {
        return BotState.COLLECTING_GROUP_STATISTICS;
    }
}
