package com.example.tinkoffbot.bot;

import com.example.tinkoffbot.bot.handlers.MessageHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

@Component
public class BotStateUnifier {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TelegramProcessing.class);

    private HashMap<BotState, MessageHandler> messageHandlerMap = new HashMap<>();

    public BotStateUnifier(List<MessageHandler> messageHandlers) {
        messageHandlers.forEach(messageHandler ->
                this.messageHandlerMap.put(
                        messageHandler.getHandleName(),
                        messageHandler
                )
        );
    }

    public SendMessage execute(BotState currentState, Message message) throws IOException, GeneralSecurityException {
        MessageHandler currentMessageHandler = findMessageHandler(currentState);
        log.info("Starting handling MessageHandler: {}, Message: {}",
                currentMessageHandler.getHandleName().toString(),
                message.getFrom().getId());
        return currentMessageHandler.handle(message);
    }

    private MessageHandler findMessageHandler(BotState currentState) {
        log.info("Trying to find in messageHandlerMap BotState: {}", currentState);
        return messageHandlerMap.get(currentState);
    }
}