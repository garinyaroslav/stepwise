package com.github.stepwise.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadConfig {

  private List<String> allowedMimeTypes;

}
