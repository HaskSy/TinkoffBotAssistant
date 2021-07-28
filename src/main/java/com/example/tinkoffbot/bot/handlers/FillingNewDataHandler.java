package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.cache.UserDataCache;
import com.example.tinkoffbot.model.UserData;
import com.example.tinkoffbot.service.ButtonService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class FillingNewDataHandler implements MessageHandler {

    private UserDataCache userDataCache;
    private ButtonService buttonService;

    public FillingNewDataHandler(UserDataCache userDataCache, ButtonService buttonService) {
        this.userDataCache = userDataCache;
        this.buttonService = buttonService;
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
                reply = buttonService.getCancelMessage(chatId, "Введите имя (Просто пример данных, которые будут записываться в базу)");
                userDataCache.setUserCurrentBotState(userId, BotState.SHOW_KEYBOARD_YES_NO);
                break;
            case SHOW_KEYBOARD_YES_NO:
                if (text.equals("Отмена")) {
                    reply = buttonService.getMainMenuMessage(chatId, "Вы будете возвращены в главное меню");
                    userData = null;
                    userDataCache.setUserCurrentBotState(userId, BotState.SHOW_KEYBOARD_MAIN_MENU);
                } else {
                    userData.setFirstName(text);
                    reply = buttonService.getYesNoMessage(chatId, String.format("Проверьте, что данные введены верно! \n Имя: %s", userData.getFirstName()));
                    userDataCache.setUserCurrentBotState(userId, BotState.FILLING_NEW_DATA_COMPLETE);
                }
                break;
            case FILLING_NEW_DATA_COMPLETE:

                if (text.equals("Да")) {
                    userDataCache.setUserCurrentBotState(userId, BotState.SHOW_KEYBOARD_MAIN_MENU);
                    reply = buttonService.getMainMenuMessage(chatId, "Данные были добавлены в базу (пока нет)");
                } else if (text.equals("Нет")) {
                    userDataCache.setUserCurrentBotState(userId, BotState.SHOW_KEYBOARD_YES_NO);
                    reply = buttonService.getCancelMessage(chatId, "Данные будет предложено ввести заново! \n Введите имя");
                } else {
                    reply = new SendMessage(chatId, "Пожалуйста, воспользуйтесь панелью");
                }
                break;
        }

        userDataCache.saveUserProfileData(userId, userData);

        return reply;

    }

}