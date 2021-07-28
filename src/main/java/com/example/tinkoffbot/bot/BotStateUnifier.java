package com.example.tinkoffbot.bot;

import com.example.tinkoffbot.bot.handlers.MessageHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

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

    public SendMessage execute(BotState currentState, Message message) {
        MessageHandler currentMessageHandler = findMessageHandler(currentState);
        log.info("Starting handling MessageHandler: {}, Message: {}",
                currentMessageHandler.getHandleName().toString(),
                message.getFrom().getId());
        return currentMessageHandler.handle(message);
    }

    private MessageHandler findMessageHandler(BotState currentState) {
        log.info("Trying to find in messageHandlerMap BotState: {}", currentState);
        if (isFillingNewData(currentState)) {
            return messageHandlerMap.get(BotState.FILLING_NEW_DATA);
        }
        return messageHandlerMap.get(currentState);
    }

    private boolean isFillingNewData(BotState currentState) {
        switch (currentState) {
            case ASK_FIRST_NAME:
            case SHOW_KEYBOARD_YES_NO:
            case FILLING_NEW_DATA_COMPLETE:
                return true;
            default:
                return false;
        }
    }

}