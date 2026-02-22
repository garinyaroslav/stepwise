package com.github.stepwise.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "client")
public class ClientConfigurationProperties {

    private String url;

    private String refreshTokenUrl;

}
