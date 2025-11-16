package com.github.stepwise.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.configuration.MinioConfig;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    private final MinioClient minioClient;

    private final MinioConfig minioConfig;

    @PostConstruct
    public void init() {
        try {
            for (String bucketName : minioConfig.getBucketNames())
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("Buckets creating exception: " + e.getMessage());
        }
    }

    public void uploadExplanatoryFile(Long studentId, Long projectId, Long itemId, MultipartFile file)
            throws Exception {
        String worksBucketName = minioConfig.getBucketNames().get(0);

        String objectName = String.format("%d/%d/%d/%s", studentId, projectId, itemId, file.getOriginalFilename());

        minioClient.putObject(PutObjectArgs.builder().bucket(worksBucketName).object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType())
                .build());
    }

    public void deleteExplanatoryFile(Long studentId, Long projectId, Long itemId, String filename)
            throws Exception {
        String worksBucketName = minioConfig.getBucketNames().get(0);

        String objectName = String.format("%d/%d/%d/%s", studentId, projectId, itemId, filename);

        minioClient.removeObject(RemoveObjectArgs.builder().bucket(worksBucketName).object(objectName).build());
    }

    public InputStream downloadExplanatoryFile(Long studentId, Long projectId, Long itemId,
            String filename) throws Exception {
        String worksBucketName = minioConfig.getBucketNames().get(0);

        String objectName = String.format("%d/%d/%d/%s", studentId, projectId, itemId, filename);

        return minioClient
                .getObject(GetObjectArgs.builder().bucket(worksBucketName).object(objectName).build());
    }
}
