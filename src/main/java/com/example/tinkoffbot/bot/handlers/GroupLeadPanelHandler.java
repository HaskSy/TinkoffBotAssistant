package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.service.ButtonService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class GroupLeadPanelHandler implements MessageHandler {

    private final ButtonService buttonService;

    public GroupLeadPanelHandler(ButtonService buttonService) {
        this.buttonService = buttonService;
    }

    @Override
    public SendMessage handle(Message message) {
        return buttonService.getGroupPanelMessage(message.getChatId(), "Пожалуйста воспользуйтесь панелью");
    }

    @Override
    public BotState getHandleName() {
        return BotState.SHOW_KEYBOARD_GROUP_LEAD;
    }
}
