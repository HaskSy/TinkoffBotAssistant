package com.example.tinkoffbot.config;

import com.example.tinkoffbot.bot.TelegramBot;
import com.example.tinkoffbot.bot.TelegramProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;

@Configuration
public class BotConfig {
    @Value("${telegrambot.webHookPath}")
    private String webHookPath;
    @Value("${telegrambot.botUsername}")
    private String botUsername;
    @Value("${telegrambot.botToken}")
    private String botToken;

    @Bean
    public TelegramBot telegramBot(TelegramProcessing telegramProcessing) {
        DefaultBotOptions options = ApiContext
                .getInstance(DefaultBotOptions.class);

        TelegramBot telegramBot = new TelegramBot(options, telegramProcessing);
        telegramBot.setBotUsername(botUsername);
        telegramBot.setBotToken(botToken);
        telegramBot.setWebHookPath(webHookPath);

        return telegramBot;
    }

}