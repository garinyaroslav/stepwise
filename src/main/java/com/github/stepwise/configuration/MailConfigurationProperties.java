package com.github.stepwise.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfigurationProperties {

    private String host;

    private int port;

    private String username;

    private String password;

}
