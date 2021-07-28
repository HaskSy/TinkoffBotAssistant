package com.example.tinkoffbot.cache;

import com.example.tinkoffbot.bot.BotState;
import com.example.tinkoffbot.model.UserData;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class UserDataCache implements DataCache {


    private HashMap<Integer, BotState> usersBotStateMap = new HashMap<>();
    private HashMap<Integer, UserData> userDataMap = new HashMap<>();

    @Override
    public void setUserCurrentBotState(int userId, BotState botState) {
        log.info("New BotState: {} for User: {}",
                botState, userId);
        usersBotStateMap.put(userId, botState);
    }

    @Override
    public BotState getUserCurrentBotState(int userId) {
        BotState botState = usersBotStateMap.get(userId);
        if (botState == null) {
            botState = BotState.START;
        }
        log.info("Current BotState for User: {} is {}",
                userId, botState);
        return botState;
    }


    @Override
    public UserData getUserProfileData(int userId) {
        UserData userProfileData = userDataMap.get(userId);
        if (userProfileData == null) {
            userProfileData = new UserData();
        }
        log.info("User: {} UserDate: {}", userId, userProfileData);
        return userProfileData;
    }

    @Override
    public void saveUserProfileData(int userId, UserData userData) {
        userDataMap.put(userId, userData);
        log.info("New data was set to cache User: {} UserDate: {}", userId, userData);

    }
}
