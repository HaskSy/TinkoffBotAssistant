package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {

    Logger log = org.slf4j.LoggerFactory.getLogger(MessageHandler.class);

    SendMessage handle(Message message);

    BotState getHandleName();
}
