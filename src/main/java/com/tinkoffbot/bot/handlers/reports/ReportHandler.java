package com.tinkoffbot.bot.handlers.reports;

import com.tinkoffbot.services.ErrorEnum;
import com.tinkoffbot.bot.BotState;
import com.tinkoffbot.bot.handlers.MessageHandler;
import com.tinkoffbot.model.ReportData;
import com.tinkoffbot.services.GoogleServices;
import com.vdurmont.emoji.EmojiParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class ReportHandler implements MessageHandler {

    private ReportData collectData(String text) {
        String[] message_array = text.split("\n", 7);
        ReportData data = new ReportData();
        data.setFio(message_array[0].substring(10));
        data.setActivityId(message_array[3].substring(16));
        data.setQuestion(message_array[6].substring(11));
        return data;
    }

    private static boolean checkIfReportMessage(String text) {
        String[] message_array = text.split("\n", 7);
        return message_array.length == 7 &&
                message_array[0].startsWith("Привет,") &&
                message_array[1].startsWith("Логин -") &&
                message_array[2].startsWith("Клиент -") &&
                message_array[3].startsWith("Id активности -") &&
                message_array[4].startsWith("Адрес встречи -") &&
                message_array[5].equals("") &&
                message_array[6].startsWith("Мой вопрос:");
    }

    @Override
    public SendMessage handle(@NotNull Message message) throws IOException, GeneralSecurityException {

        if (!checkIfReportMessage(message.getText())) {
            return new SendMessage(message.getChatId(), "Данные введены неверно ".concat(EmojiParser.parseToUnicode(":x:")));
        }
        GoogleServices.setFolderId();

        ReportData data = collectData(message.getText());

        ErrorEnum error = GoogleServices.processReportMessage(data, message.getDate());
        if (error == null) {
            return new SendMessage(message.getChatId(), "Данные были успешно записаны ".concat(EmojiParser.parseToUnicode(":white_check_mark:")));
        }
        if (error.equals(ErrorEnum.SAME_ACTIVITY_ID_ERROR)) {
            return new SendMessage(message.getChatId(), "Данные с данным ID активности: ".concat(data.getActivityId()).concat(" уже были внесены ").concat(EmojiParser.parseToUnicode(":x:")));
        }
        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.REPORT_COLLECTING;
    }
}
