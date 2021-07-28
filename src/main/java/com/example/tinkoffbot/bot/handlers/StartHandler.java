package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import com.example.tinkoffbot.service.ButtonService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartHandler implements MessageHandler {

    private UserDataCache userDataCache;
    private ButtonService buttonService;

    public StartHandler(UserDataCache userDataCache, ButtonService buttonService) {
        this.userDataCache = userDataCache;
        this.buttonService = buttonService;
    }

    @Override
    public SendMessage handle(Message message) {
        if (userDataCache.getUserCurrentBotState(message.getFrom().getId()).equals(BotState.START)) {
            userDataCache.setUserCurrentBotState(message.getFrom().getId(), BotState.SHOW_KEYBOARD_MAIN_MENU);
        }
        return processUsersInput(message);
    }

    @Override
    public BotState getHandleName() {
        return BotState.START;
    }

    private SendMessage processUsersInput(Message message) {
        int userId = message.getFrom().getId();
        long chatId = message.getChatId();

        SendMessage reply = buttonService.getMainMenuMessage(chatId, "Привет, я бот неОлег. Я отвечаю только тем представителям, которые находятся в базе данных. Ну что, приступим?");
        userDataCache.setUserCurrentBotState(userId, BotState.SHOW_KEYBOARD_MAIN_MENU);
        return reply;


    }

}
