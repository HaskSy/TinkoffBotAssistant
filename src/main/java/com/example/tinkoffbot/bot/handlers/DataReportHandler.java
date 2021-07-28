package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class DataReportHandler implements MessageHandler {

    private UserDataCache userDataCache;

    public DataReportHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.SHOW_MAIN_MENU);
        return new SendMessage(message.getChatId(), "Эта кнопка позволит представителю отправить лидеру группы сообщение об ошибке в случае неверно введенных данных (с указанием на конкретные данные и предложенные исправления)");
    }

    @Override
    public BotState getHandleName() {
        return BotState.SEND_DATA_REPORT;
    }
}
