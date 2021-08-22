package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.*;
import java.util.Scanner;

@Component
public class RegisterHandler implements MessageHandler {

    private static String trim(String text) {
        String data = text.replaceFirst("/reg ", "").replaceAll("\\s+", " ");
        if (data.startsWith(" ")) {
            return data.substring(1);
        }
        return data;
    }

    @Override
    public SendMessage handle(Message message) throws IOException {
        if (message.getText().startsWith("/reg ")) {
            String data = trim(message.getText());
            String path = "src/main/resources/id-name-file";
            Scanner sc = new Scanner(path);
            sc.nextLine();
            StringBuilder buffer = new StringBuilder();
            int userID = message.getFrom().getId();

            boolean switched = false;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!switched && line.startsWith(String.valueOf(userID))) {
                    buffer.append(userID).append("->").append(data);
                    switched = true;
                }
                else {
                    buffer.append(line);
                }
                buffer.append(System.lineSeparator());
            }

            if (!switched) {
                buffer.append(userID).append("->").append(data);
            }

            sc.close();

            FileWriter writer = new FileWriter(path);
            writer.append(buffer.toString());
            writer.close(); // TODO: FLUSH and make it once open

            return new SendMessage(message.getChatId(), "Ваши данные записаны");
        }

        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.REGISTER;
    }
}
