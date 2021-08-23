package com.example.tinkoffbot.bot;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class TelegramProcessing {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramProcessing.class);

    private final BotStateUnifier botStateUnifier;

    public TelegramProcessing(BotStateUnifier botStateUnifier) {
        this.botStateUnifier = botStateUnifier;
    }

    public BotApiMethod<?> handleUpdate(Update update) throws IOException, GeneralSecurityException {
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

    private SendMessage handleInputMessage(Message message) throws IOException, GeneralSecurityException {

        if (message.getChatId() != -511906413) {
            return null;
        }

        log.info("Start handling input message from user {} with ID: {}", message.getFrom().getUserName(), message.getFrom().getId());
        String input = message.getText();
        int userId = message.getFrom().getId();
        BotState botState;
        SendMessage replyMessage;

        log.info("Input: {}", input);

        if (input.equals("/start")) {
            botState = BotState.START;

        } else if (input.startsWith("/send")) {
            botState = BotState.DATA_COLLECTING;
        } else if (input.startsWith("/reg")) {
            botState = BotState.REGISTER;
        } else if (input.equals("/help")) {
            botState = BotState.HELP;
        } else {
            log.info("Input does not fit with valid cases, getting current bot state of user with ID: {}", userId);
            botState = BotState.START;
        }

        log.info("BotState was set to: {}, case: {}",
                botState, input);


        replyMessage = botStateUnifier.execute(botState, message);

        return replyMessage;
    }
}
