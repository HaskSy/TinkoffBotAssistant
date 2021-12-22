package com.tinkoffbot.controller;

import com.tinkoffbot.bot.TelegramBot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@Tag(name="WebHookController", description="Automatically detects update on Bot settled WebHook URL")
public class WebHookController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(WebHookController.class);

    @Autowired
    private final TelegramBot telegramBot;

    public WebHookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Operation(
            summary = "Catches new messages & Starts handling",
            description = "After new message received to a Bot, HTTP callback getting triggered & and bot sends processing result" +
                    "via POST method to URL configured in TelegramBot.webHookPath field"
    )
    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        log.info("Logger caught new WebHook Update! ID: {}",
                update.getUpdateId());
        return telegramBot.onWebhookUpdateReceived(update);
    }

}
