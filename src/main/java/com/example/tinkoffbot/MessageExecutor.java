package com.example.tinkoffbot;

import com.example.tinkoffbot.bot.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MessageExecutor {

    TelegramBot telegramBot;
    private static final long chatID = -511906413;

    public MessageExecutor(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0 23 L * ?", zone = "Europe/Moscow")
    public void endOfMonthRememberNight() throws TelegramApiException {
            telegramBot.execute(new SendMessage(chatID, "Внимание! Через час закончится последний день месяца, отправляйте всю статистику как можно быстрее!"));
        }

    @Scheduled(cron = "0 0 9 L * ?", zone = "Europe/Moscow")
    public void endOfMonthRememberMorning() throws TelegramApiException {
        telegramBot.execute(new SendMessage(chatID, "Внимание! Сегодня последний день месяца, не забудьте отправить всю статистику до окончания дня!"));
    }
}
