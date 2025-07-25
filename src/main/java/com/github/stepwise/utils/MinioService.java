package com.github.stepwise.utils;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.configuration.MinioConfig;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {
  private final MinioClient minioClient;

  private final MinioConfig minioConfig;

  @PostConstruct
  public void init() {
    try {
      boolean bucketExists = minioClient
          .bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
      if (!bucketExists) {
        minioClient
            .makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
      }
    } catch (Exception e) {
      throw new RuntimeException("Ошибка при создании бакета: " + e.getMessage());
    }
  }

  public void uploadFile(MultipartFile file, String fileName) throws Exception {
    try {
      minioClient.putObject(PutObjectArgs.builder().bucket(minioConfig.getBucketName())
          .object(fileName).stream(file.getInputStream(), file.getSize(), -1)
          .contentType(file.getContentType()).build());
    } catch (MinioException e) {
      throw new Exception("Error with file uploading" + e.getMessage());
    }
  }

  public InputStream downloadFile(String fileName) throws Exception {
    try {
      return minioClient.getObject(
          GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName).build());
    } catch (MinioException e) {
      throw new Exception("Error with file downloading" + e.getMessage());
    }
  }
}
