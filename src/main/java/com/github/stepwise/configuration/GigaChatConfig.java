package com.github.stepwise.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder.OAuthBuilder;
import chat.giga.model.Scope;

@Configuration
public class GigaChatConfig {

    private String authKey;

    public GigaChatConfig(@Value("${gigaChat.authKey}") String authKey) {
        this.authKey = authKey;
    }

    public GigaChatClient gigaChatClient() {
        return GigaChatClient.builder()
                .verifySslCerts(false)
                .authClient(AuthClient.builder()
                        .withOAuth(OAuthBuilder.builder()
                                .scope(Scope.GIGACHAT_API_PERS)
                                .authKey(authKey)
                                .build())
                        .build())
                .build();
    }

}
