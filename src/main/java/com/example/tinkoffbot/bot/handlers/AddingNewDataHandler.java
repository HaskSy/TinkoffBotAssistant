package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import com.example.tinkoffbot.model.UserData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class AddingNewDataHandler implements MessageHandler {

    private UserDataCache userDataCache;

    public AddingNewDataHandler(UserDataCache userDataCache) {
        this.userDataCache = userDataCache;
    }

    @Override
    public SendMessage handle(Message message) {
        if (userDataCache.getUserCurrentBotState(message.getFrom().getId()).equals(BotState.FILLING_NEW_DATA)) {
            userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.ASK_FIRST_NAME);
        }
        return processUsersInput(message);
    }

    @Override
    public BotState getHandleName() {
        return BotState.FILLING_NEW_DATA;
    }

    private SendMessage processUsersInput(Message message) {
        String text = message.getText();
        int userId = message.getFrom().getId();
        long chatId = message.getChatId();

        UserData userData = userDataCache.getUserProfileData(userId);
        BotState botState = userDataCache.getUserCurrentBotState(userId);

        SendMessage reply = null;

        switch (botState) {
            case ASK_FIRST_NAME:
                reply = new SendMessage(chatId, "Введите имя (Просто пример данных, которые будут записываться в базу)");
                userDataCache.setUserCurrentBotState(userId, BotState.FILLED_NEW_DATA);
                break;

            case FILLED_NEW_DATA:
                userData.setFirstName(text);
                userDataCache.setUserCurrentBotState(userId, BotState.SHOW_MAIN_MENU);
                reply = new SendMessage(chatId, "Данные были добавлены в базу (пока нет)");
                break;
        }

        userDataCache.saveUserProfileData(userId, userData);

        return reply;


    }
}