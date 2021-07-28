package com.example.tinkoffbot.cache;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.model.UserData;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public interface DataCache {

    Logger log = org.slf4j.LoggerFactory.getLogger(DataCache.class);

    void setUserCurrentBotState(int userId, BotState botState);

    BotState getUserCurrentBotState(int userId);

    UserData getUserProfileData(int userId);

    void saveUserProfileData(int userId, UserData userData);
}
