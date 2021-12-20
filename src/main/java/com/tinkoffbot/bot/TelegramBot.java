package com.tinkoffbot.bot;

import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Setter
public class TelegramBot extends TelegramWebhookBot {

    private String webHookPath;
    private String botUsername;
    private String botToken;

    private final TelegramProcessing telegramProcessing;

    public TelegramBot(DefaultBotOptions botOptions, TelegramProcessing telegramProcessing) {
        super(botOptions);
        this.telegramProcessing = telegramProcessing;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return telegramProcessing.handleUpdate(update);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

}
