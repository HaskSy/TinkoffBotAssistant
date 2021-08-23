package com.tinkoffbot.bot.handlers;

import com.tinkoffbot.bot.BotState;
import com.tinkoffbot.model.UserData;
import com.tinkoffbot.services.GoogleServices;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class DataCollectingHandler implements MessageHandler {

    private static String[] trim(String text) {
        return text.replaceAll("\\s+", " ")
                .replaceFirst("/send ", "")
                .toLowerCase()
                .split("\\s|\\n");
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

            String name = GoogleServices.getNameById(message.getFrom().getId());

            if (name == null) {
                return new SendMessage(message.getChatId(), "[".concat(String.valueOf(message.getFrom().getUserName())).concat("]: Вы должны сначала записать свое ФИО через /reg"));
            }

            UserData userData = new UserData();
            userData.setName(name);

            if (dataList.length == 0 || dataList.length % 2 != 0) {
                return new SendMessage(message.getChatId(), "[".concat(name).concat("]: Данные введены неверно ").concat(EmojiParser.parseToUnicode(":x:")));
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
                    case "вс":
                        if (isInteger(dataList[index + 1])) {
                            userData.setVS(Integer.parseInt(dataList[index + 1]));
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

                List<Object> ans = GoogleServices.processSentMessage(userData, timeStamp);

                return new SendMessage(message.getChatId(), "[".concat(name).concat("]: Данные внесены ").concat(EmojiParser.parseToUnicode(":white_check_mark:")).concat(System.lineSeparator())
                        .concat("Текущая статистика за месяц:").concat(System.lineSeparator())
                        .concat("KK: ").concat(String.valueOf(ans.get(1))).concat(System.lineSeparator())
                        .concat("ДК: ").concat(String.valueOf(ans.get(2))).concat(System.lineSeparator())
                        .concat("ТИ: ").concat(String.valueOf(ans.get(3))).concat(System.lineSeparator())
                        .concat("СИМ: ").concat(String.valueOf(ans.get(4))).concat(System.lineSeparator())
                        .concat("МНП: ").concat(String.valueOf(ans.get(5)).concat(System.lineSeparator())
                        .concat("ВС: ").concat(String.valueOf(ans.get(6))))
                );
            }
            return new SendMessage(message.getChatId(), "[".concat(name).concat("]: Данные введены неверно ").concat(EmojiParser.parseToUnicode(":x:")));
        }
        return null;
    }

    @Override
    public BotState getHandleName() {
        return BotState.DATA_COLLECTING;
    }

}
