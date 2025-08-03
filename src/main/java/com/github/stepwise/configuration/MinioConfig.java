package com.github.stepwise.configuration;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

  private String url;

  private String accessKey;

  private String secretKey;

  private List<String> bucketNames = new ArrayList<String>();

  @Bean
  public MinioClient minioClient() throws Exception {

    return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();

  }

}
