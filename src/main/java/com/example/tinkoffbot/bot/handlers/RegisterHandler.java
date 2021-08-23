package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.services.GoogleSheetsService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Scanner;

@Component
public class RegisterHandler implements MessageHandler {

    private static String trim(String text) {

        return text.replaceAll("\\s+", " ").replaceFirst("/reg ", "");
    }

    private static void addNameToFile(String path, int userID, String name, int timestamp) throws IOException, GeneralSecurityException {
        Scanner sc = new Scanner(new File(path));
        StringBuilder buffer = new StringBuilder();

        boolean switched = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!switched && line.startsWith(String.valueOf(userID))) {
                buffer.append(userID).append("->").append(name);
                GoogleSheetsService.updateName(line.split("->")[1], name, GoogleSheetsService.getCurrentSpreadsheet(timestamp));
                switched = true;
            }
            else {
                buffer.append(line);
            }
            buffer.append(System.lineSeparator());
        }

        if (!switched) {
            buffer.append(userID).append("->").append(name);
        }

        sc.close();

        FileWriter writer = new FileWriter(path);
        writer.append(buffer.toString());
        writer.close();
    }

    @Override
    public SendMessage handle(Message message) throws IOException, GeneralSecurityException {

        String textMessage = message.getText();

        if (textMessage.startsWith("/reg ") || textMessage.startsWith("/reg\n")) {

            String path = "src/main/resources/id-name-file";
            int userID = message.getFrom().getId();
            String name = trim(textMessage);

            addNameToFile(path, userID, name, message.getDate());


            return new SendMessage(message.getChatId(), "Ваши данные записаны");
        }

        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.REGISTER;
    }
}
