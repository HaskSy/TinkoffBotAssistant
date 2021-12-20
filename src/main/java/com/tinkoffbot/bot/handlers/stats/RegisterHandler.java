package com.tinkoffbot.bot.handlers.stats;

import com.tinkoffbot.bot.BotState;
import com.tinkoffbot.bot.handlers.MessageHandler;
import com.tinkoffbot.services.GoogleServices;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.*;
import java.security.GeneralSecurityException;

@Component
public class RegisterHandler implements MessageHandler {

    private static String trim(String text) {

        return text.replaceAll("\\s+", " ").replaceFirst("/reg ", "");
    }

    @Override
    public SendMessage handle(Message message) throws IOException, GeneralSecurityException {

        String textMessage = message.getText();

        if (textMessage.startsWith("/reg ") || textMessage.startsWith("/reg\n")) {

            int userID = message.getFrom().getId();
            String name = trim(textMessage);

            GoogleServices.processRegMessage(userID, name, message.getDate());

            return new SendMessage(message.getChatId(), "Ваше ФИО записано ".concat(EmojiParser.parseToUnicode(":white_check_mark:")));
        }

        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.REGISTER;
    }
}
