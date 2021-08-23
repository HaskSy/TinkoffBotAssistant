package com.tinkoffbot;

import com.tinkoffbot.bot.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MessageExecutor {

    TelegramBot telegramBot;

    public MessageExecutor(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0 23 L JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV ?", zone = "Europe/Moscow")
    public void endOfMonthRememberNight() throws TelegramApiException {
        String hourMessage = "Внимание! Через час закончится последний день РП, отправляйте всю статистику как можно быстрее!";
        telegramBot.execute(new SendMessage(Long.valueOf(System.getenv("GROUP_CHAT_ID")), hourMessage));
    }

    @Scheduled(cron = "0 0 23 19 DEC ?", zone = "Europe/Moscow")
    public void endOfDecemberRememberNight() throws TelegramApiException {
        String hourMessage = "Внимание! Через час закончится последний день РП, отправляйте всю статистику как можно быстрее!";
        telegramBot.execute(new SendMessage(Long.valueOf(System.getenv("GROUP_CHAT_ID")), hourMessage));
    }

    @Scheduled(cron = "0 0 9 L JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV ?", zone = "Europe/Moscow")
    public void endOfMonthRememberMorning() throws TelegramApiException {
        String dayMessage = "Внимание! Сегодня последний день РП, не забудьте отправить всю статистику до окончания дня!";
        telegramBot.execute(new SendMessage(Long.valueOf(System.getenv("GROUP_CHAT_ID")), dayMessage));
    }

    @Scheduled(cron = "0 0 23 19 DEC ?", zone = "Europe/Moscow")
    public void endOfDecemberRememberMorning() throws TelegramApiException {
        String hourMessage = "Внимание! Через час закончится последний день РП, отправляйте всю статистику как можно быстрее!";
        telegramBot.execute(new SendMessage(Long.valueOf(System.getenv("GROUP_CHAT_ID")), hourMessage));
    }

}
