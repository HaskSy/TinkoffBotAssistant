package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.service.ButtonService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MainMenuHandler implements MessageHandler {

    private final ButtonService buttonService;

    public MainMenuHandler(ButtonService buttonService) {
        this.buttonService = buttonService;
    }

    @Override
    public SendMessage handle(Message message) {
        return buttonService.getMainMenuMessage(message.getChatId(), "Пожалуйста воспользуйтесь панелью");
    }

    @Override
    public BotState getHandleName() {
        return BotState.SHOW_MAIN_MENU;
    }

}