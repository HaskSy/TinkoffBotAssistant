package com.tinkoffbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String description = "Bot configured for a single chat to parse messages from Telegram and collecting data onto Google Drive";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tinkoff Bot Assistant")
                        .version("1.0.0")
                        .contact(new Contact()
                                .email("example@gmail.com")
                                .name("Some Name")
                                .url("https://github.com/HaskSy"))
                        .description(description)
                );
    }

}