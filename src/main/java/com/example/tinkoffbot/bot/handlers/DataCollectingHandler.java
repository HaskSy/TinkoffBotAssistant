package com.example.tinkoffbot.bot.handlers;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.model.UserData;
import com.example.tinkoffbot.services.GoogleSheetsService;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Scanner;

@Component
public class DataCollectingHandler implements MessageHandler {

    private static String[] trim(String text) {
        return text.replaceAll("\\s+", " ")
                .replaceFirst("/send ", "")
                .toLowerCase()
                .split("\\s|\\n");
    }

    private static String getNameById(String path, int userId) throws FileNotFoundException {

        Scanner sc = new Scanner(new File(path));

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.startsWith(String.valueOf(userId))) {
                sc.close();
                return line.split("->")[1];
            }
        }

        sc.close();
        return null;
    }

    private static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    @Override
    public SendMessage handle(Message message) throws IOException, GeneralSecurityException {

        String textMessage = message.getText();

        if (textMessage.startsWith("/send ") || message.getText().startsWith("/send\n")) {

            String[] dataList = trim(textMessage);
            String path = "src/main/resources/id-name-file";

            String name = getNameById(path, message.getFrom().getId());

            if (name == null) {
                return new SendMessage(message.getChatId(), "[" + message.getFrom().getUserName() +"]: Вы должны сначала записать свое ФИО через /reg");
            }

            UserData userData = new UserData();
            userData.setName(name);

            if (dataList.length % 2 != 0) {
                return new SendMessage(message.getChatId(), "[" + name + "]: Данные введены неверно " + EmojiParser.parseToUnicode(":x:"));
            }

            boolean correctFlag = true;

            label:
            for (int i = 0; i < dataList.length / 2; i++) {
                int index = i * 2;
                switch (dataList[index]) {
                    case "кк":
                        if (isInteger(dataList[index + 1])) {
                            userData.setKK(Integer.parseInt(dataList[index + 1]));
                        } else {
                            correctFlag = false;
                            break label;
                        }
                        break;
                    case "дк":
                        if (isInteger(dataList[index + 1])) {
                            userData.setDK(Integer.parseInt(dataList[index + 1]));
                        } else {
                            correctFlag = false;
                            break label;
                        }
                        break;
                    case "ти":
                        if (isInteger(dataList[index + 1])) {
                            userData.setTI(Integer.parseInt(dataList[index + 1]));
                        } else {
                            correctFlag = false;
                            break label;
                        }
                        break;
                    case "сим":
                        if (isInteger(dataList[index + 1])) {
                            userData.setSIM(Integer.parseInt(dataList[index + 1]));
                        } else {
                            correctFlag = false;
                            break label;
                        }
                        break;
                    case "мнп":
                        if (isInteger(dataList[index + 1])) {
                            userData.setMNP(Integer.parseInt(dataList[index + 1]));
                        } else {
                            correctFlag = false;
                            break label;
                        }
                        break;
                    default:
                        correctFlag = false;
                        break;
                }
            }

            if (correctFlag) {


                Integer timeStamp = message.getDate();

                List<Object> ans = GoogleSheetsService.processSentMessage(userData, timeStamp);

                return new SendMessage(message.getChatId(), "[" + name + "]: Данные внесены " + EmojiParser.parseToUnicode(":white_check_mark:") + System.lineSeparator()  +
                        "Текущая статистика за месяц:" + System.lineSeparator() +
                        "KK: " + ans.get(1) + System.lineSeparator() +
                        "ДК: " + ans.get(2) + System.lineSeparator() +
                        "ТИ: " + ans.get(3) + System.lineSeparator() +
                        "СИМ: " + ans.get(4) + System.lineSeparator() +
                        "МНП: " + ans.get(5));
            }
            return new SendMessage(message.getChatId(), "[" + name + "]: Данные введены неверно " + EmojiParser.parseToUnicode(":x:"));
        }
        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.DATA_COLLECTING;
    }

}
