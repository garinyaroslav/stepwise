package com.github.stepwise.configuration;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
  private String url;
  private String accessKey;
  private String secretKey;
  private String bucketName;

  @Bean
  public MinioClient minioClient() throws Exception {
    MinioClient minioClient =
        MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();

    minioClient.makeBucket(MakeBucketArgs.builder().bucket("works").build());

    return minioClient;

  }
}
