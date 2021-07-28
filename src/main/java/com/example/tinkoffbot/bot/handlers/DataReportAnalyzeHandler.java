package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class DataReportAnalyzeHandler implements MessageHandler {

    private UserDataCache userDataCache;

    public DataReportAnalyzeHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.SHOW_GROUP_LEAD_PANEL);
        return new SendMessage(message.getChatId(), "Сюда лидеру группы будет приходить список запросов на изменение данных в случае ошибки");
    }

    @Override
    public BotState getHandleName() {
        return BotState.ANALYSING_DATA_REPORTS;
    }
}
