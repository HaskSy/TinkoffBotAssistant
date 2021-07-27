package com.example.tinkoffbot.controller;

import com.example.tinkoffbot.bot.TelegramBot;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebHookController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(WebHookController.class);

    @Autowired
    private final TelegramBot telegramBot;

    public WebHookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        log.info("Logger caught new WebHook Update! ID: {}",
                update.getUpdateId());
        return telegramBot.onWebhookUpdateReceived(update);
    }

//    @GetMapping("/setWebHook/{url}")
//    public WebHookResponse getWebHook(
//            @PathVariable("url") String url) throws IOException {
//        return telegramBot.setRetrofitWebhook(url);
//    }

}
