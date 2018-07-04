package com.viber.bot.sample;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.Futures;
import com.viber.bot.Request;
import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.api.ViberBot;
import com.viber.bot.message.TextMessage;
import com.viber.bot.profile.BotProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@RestController
@SpringBootApplication
public class SpringEchoBot implements ApplicationListener<ApplicationReadyEvent> {

    @Inject
    private ViberBot bot;
    String [] words = {"Как дела?!","Привет!","Пока!","Ок","Зачем?","Нет, уходи",
        "Плати мне деньги", "Всмысле", "эээ", "(money)", "(mad)", "(sick)", 
       "Подумай над смыслом своей жизни, пожалуйста", "не понимаю о чем ты..",
        "(flirt)", "http://www.mental-skills.ru/upload/iblock/3fa/bot.jpg", "минуточку", 
     "https://evilcoder.ru/wp-content/uploads/2017/07/bot-1-1024x502.jpg", 
    "http://mediasat.info/wp-content/uploads/2017/02/Bot-traffic.jpg", "(cry)", "(scream)"};


    @Inject

    private ViberSignatureValidator signatureValidator;

    @Value("${application.viber-bot.webhook-url}")
    private String webhookUrl;

    public static void main(String[] args) {
        SpringApplication.run(SpringEchoBot.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent appReadyEvent) {
        try {
            bot.setWebhook(webhookUrl).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ViberMessage viberMessage = new ViberMessage(words.length);

        bot.onMessageReceived((event, message, response) -> response.send(words[(viberMessage.getran())])); // echos everything back
        bot.onConversationStarted(event -> Futures.immediateFuture(Optional.of( // send 'Hi UserName' when conversation is started
                new TextMessage("Hi " + event.getUser().getName()))));
    }

    @PostMapping(value = "/", produces = "application/json")
    public String incoming(@RequestBody String json,
                           @RequestHeader("X-Viber-Content-Signature") String serverSideSignature)
            throws ExecutionException, InterruptedException, IOException {
        Preconditions.checkState(signatureValidator.isSignatureValid(serverSideSignature, json), "invalid signature");
        @Nullable InputStream response = bot.incoming(Request.fromJsonString(json)).get();
        return response != null ? CharStreams.toString(new InputStreamReader(response, Charsets.UTF_16)) : null;
    }

    @Configuration
    public class BotConfiguration {

        @Value("${application.viber-bot.auth-token}")
        private String authToken;

        @Value("${application.viber-bot.name}")
        private String name;

        @Nullable
        @Value("${application.viber-bot.avatar:@null}")
        private String avatar;



        @Bean
        ViberBot viberBot() {
            return new ViberBot(new BotProfile(name, avatar), authToken);
        }

        @Bean
        ViberSignatureValidator signatureValidator() {
            return new ViberSignatureValidator(authToken);
        }
    }
}
