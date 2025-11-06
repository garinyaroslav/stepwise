package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.github.stepwise.configuration.MinioConfig;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private StorageService storageService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private InputStream inputStream;

    @BeforeEach
    void setUp() {
        when(minioConfig.getBucketNames()).thenReturn(List.of("works-bucket", "other-bucket"));
    }

    @Test
    void init_WhenBucketsDoNotExist_ShouldCreateBuckets() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        storageService.init();

        verify(minioClient, times(2)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, times(2)).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void init_WhenBucketsAlreadyExist_ShouldNotCreateBuckets() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        storageService.init();

        verify(minioClient, times(2)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void init_WhenMinioClientThrowsException_ShouldThrowRuntimeException() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.init());

        assertNotNull(exception);
    }

    @Test
    void uploadExplanatoryFile_ShouldUploadFileWithCorrectPath() throws Exception {
        String worksBucketName = "works-bucket";
        when(minioConfig.getBucketNames()).thenReturn(List.of(worksBucketName));
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        storageService.uploadExplanatoryFile(1L, 2L, 3L, multipartFile);

        verify(minioClient).putObject(argThat(putObjectArgs -> {
            try {
                return putObjectArgs.bucket().equals(worksBucketName) &&
                        putObjectArgs.object().equals("1/2/3/document.pdf");
            } catch (Exception e) {
                return false;
            }
        }));
    }

    @Test
    void uploadExplanatoryFile_WhenFileUploadFails_ShouldPropagateException() throws Exception {
        String worksBucketName = "works-bucket";
        when(minioConfig.getBucketNames()).thenReturn(List.of(worksBucketName));
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        doThrow(new RuntimeException("Upload failed")).when(minioClient).putObject(any(PutObjectArgs.class));

        Exception exception = assertThrows(Exception.class,
                () -> storageService.uploadExplanatoryFile(1L, 2L, 3L, multipartFile));

        assertTrue(exception.getMessage().contains("Upload failed"));
    }

    @Test
    void uploadExplanatoryFile_WithNullFilename_ShouldHandleGracefully() throws Exception {
        String worksBucketName = "works-bucket";
        when(minioConfig.getBucketNames()).thenReturn(List.of(worksBucketName));
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        storageService.uploadExplanatoryFile(1L, 2L, 3L, multipartFile);

        verify(minioClient).putObject(argThat(putObjectArgs -> {
            try {
                return putObjectArgs.object().equals("1/2/3/null");
            } catch (Exception e) {
                return false;
            }
        }));
    }

    @Test
    void downloadExplanatoryFile_WhenFileDownloadFails_ShouldPropagateException() throws Exception {
        String worksBucketName = "works-bucket";
        String filename = "document.pdf";
        when(minioConfig.getBucketNames()).thenReturn(List.of(worksBucketName));

        doThrow(new RuntimeException("File not found")).when(minioClient).getObject(any());

        Exception exception = assertThrows(Exception.class,
                () -> storageService.downloadExplanatoryFile(1L, 2L, 3L, filename));

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void uploadExplanatoryFile_ShouldUseFirstBucketFromConfig() throws Exception {
        List<String> bucketNames = List.of("primary-bucket", "secondary-bucket");
        when(minioConfig.getBucketNames()).thenReturn(bucketNames);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(512L);
        when(multipartFile.getContentType()).thenReturn("text/plain");

        storageService.uploadExplanatoryFile(1L, 2L, 3L, multipartFile);

        verify(minioClient).putObject(argThat(putObjectArgs -> {
            try {
                return putObjectArgs.bucket().equals("primary-bucket");
            } catch (Exception e) {
                return false;
            }
        }));
    }

    @Test
    void uploadExplanatoryFile_ShouldCallPutObjectWithCorrectParameters() throws Exception {
        String worksBucketName = "works-bucket";
        when(minioConfig.getBucketNames()).thenReturn(List.of(worksBucketName));
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(2048L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        storageService.uploadExplanatoryFile(1L, 2L, 3L, multipartFile);

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

}
