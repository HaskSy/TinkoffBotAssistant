package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface MessageHandler {

    Logger log = org.slf4j.LoggerFactory.getLogger(MessageHandler.class);

    SendMessage handle(Message message) throws IOException, GeneralSecurityException;

    BotState getHandleName();
}
