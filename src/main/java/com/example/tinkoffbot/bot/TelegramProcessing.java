package com.example.tinkoffbot.bot;

import com.example.tinkoffbot.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramProcessing {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramProcessing.class);

    private BotStateUnifier botStateUnifier;
    private UserDataCache userDataCache;

    public TelegramProcessing(BotStateUnifier botStateUnifier, UserDataCache userDataCache) {
        this.botStateUnifier = botStateUnifier;
        this.userDataCache = userDataCache;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        SendMessage replyMessage = null;

        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                log.info("New message user: {}, user_id: {}, chat_id: {}, text: {}",
                        message.getFrom().getUserName(), message.getFrom().getId(), message.getChatId(), message.getText());
                replyMessage = handleInputMessage(message);
            }
        }
        return replyMessage;
    }

    private SendMessage handleInputMessage(Message message) {

        log.info("Start handling input message from user {} with ID: {}", message.getFrom().getUserName(), message.getFrom().getId());
        String input = message.getText();
        int userId = message.getFrom().getId();
        BotState currentBotState = userDataCache.getUserCurrentBotState(userId);
        BotState botState;
        SendMessage replyMessage;

        log.info("Input: {}", input);

        if (input.equals("/start")) {
            botState = BotState.START;


            /// --- MAIN MENU PANEL --- ///

        } else if (input.equals("Ввести новые данные") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_MAIN_MENU) || currentBotState.equals(BotState.FILLING_NEW_DATA))) {
            botState = BotState.FILLING_NEW_DATA;
        } else if (input.equals("Ошибка в данных") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_MAIN_MENU) || currentBotState.equals(BotState.REPORT_FILLING_DATA_ERROR))) {
            botState = BotState.REPORT_FILLING_DATA_ERROR;
        } else if (input.equals("Статистика") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_MAIN_MENU) || currentBotState.equals(BotState.COLLECTING_USER_STATISTICS))) {
            botState = BotState.COLLECTING_USER_STATISTICS;
        } else if (input.equals("Помощь") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_MAIN_MENU) || currentBotState.equals(BotState.SEND_HELP_MESSAGE_ASSISTANT))) {
            botState = BotState.SEND_HELP_MESSAGE_ASSISTANT;
        } else if (input.equals("Панель лидера группы") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_MAIN_MENU) || currentBotState.equals(BotState.SHOW_KEYBOARD_GROUP_LEAD))) {
            botState = BotState.SHOW_KEYBOARD_GROUP_LEAD;

            /// !--- MAIN MENU PANEL --- ///

            /// --- GROUP LEAD PANEL --- ///


        } else if (input.equals("Статистика группы") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_GROUP_LEAD) || currentBotState.equals(BotState.COLLECTING_GROUP_STATISTICS))) {
            botState = BotState.COLLECTING_GROUP_STATISTICS;
        } else if (input.equals("Анализ Data Report-ов") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_GROUP_LEAD) || currentBotState.equals(BotState.ANALYSING_DATA_REPORTS))) {
            botState = BotState.ANALYSING_DATA_REPORTS;
        } else if (input.equals("Что-то еще") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_GROUP_LEAD) || currentBotState.equals(BotState.SOMETHING_ELSE))) {
            botState = BotState.SHOW_KEYBOARD_GROUP_LEAD;
        } else if (input.equals("Помощь") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_GROUP_LEAD) || currentBotState.equals(BotState.SEND_HELP_MESSAGE_GROUP_LEAD))) {
            botState = BotState.SEND_HELP_MESSAGE_GROUP_LEAD;
        } else if (input.equals("Назад") &&
                (currentBotState.equals(BotState.SHOW_KEYBOARD_GROUP_LEAD) || currentBotState.equals(BotState.BACK_TO_MAIN_MENU))) {
            botState = BotState.SHOW_KEYBOARD_MAIN_MENU;

            /// !--- GROUP LEAD PANEL --- ///

        } else {
            log.info("Input does not fit with valid cases, getting current bot state of user with ID: {}", userId);
            botState = currentBotState;
        }

        log.info("BotState was set to: {}, case: {}",
                botState, input);

        userDataCache.setUserCurrentBotState(userId, botState);

        replyMessage = botStateUnifier.execute(botState, message);

        return replyMessage;
    }
}
