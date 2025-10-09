package com.ntdoc.notangdoccore.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DigitalOceanSpacesServiceTest {
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;
    @InjectMocks
    private DigitalOceanSpacesService spacesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spacesService = new DigitalOceanSpacesService(s3Client, s3Presigner);
        // 反射注入 bucketName
        try {
            java.lang.reflect.Field field = DigitalOceanSpacesService.class.getDeclaredField("bucketName");
            field.setAccessible(true);
            field.set(spacesService, "test-bucket");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testUploadFile_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello world".getBytes());
        PutObjectResponse response = PutObjectResponse.builder().eTag("etag").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(response);
        String key = spacesService.uploadFile(file, "user1");
        assertNotNull(key);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> spacesService.uploadFile(file, "user1"));
    }

    @Test
    void testUploadFile_IOException() throws IOException {
        MultipartFile file = mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenThrow(new IOException("IO error"));
        assertThrows(RuntimeException.class, () -> spacesService.uploadFile(file, "user1"));
    }

    @Test
    void testGenerateDownloadUrl_Success() {
        String key = "documents/user1/2025/10/test.txt";
        URL url = mock(URL.class);
        software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presigned = mock(software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(url);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);
        URL result = spacesService.generateDownloadUrl(key, Duration.ofMinutes(10));
        assertEquals(url, result);
    }

    @Test
    void testFileExists_True() {
        String key = "documents/user1/2025/10/test.txt";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
        assertTrue(spacesService.fileExists(key));
    }

    @Test
    void testFileExists_False() {
        String key = "documents/user1/2025/10/test.txt";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertFalse(spacesService.fileExists(key));
    }

    @Test
    void testDeleteFile_Success() {
        String key = "documents/user1/2025/10/test.txt";
        DeleteObjectResponse response = DeleteObjectResponse.builder().build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(response);
        assertTrue(spacesService.deleteFile(key));
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteFile_Exception() {
        String key = "documents/user1/2025/10/test.txt";
        doThrow(S3Exception.builder().build()).when(s3Client).deleteObject(any(DeleteObjectRequest.class));
        assertFalse(spacesService.deleteFile(key));
    }

    @Test
    void testGenerateStoragePath() {
        String path = spacesService.generateStoragePath("user1", "test.txt");
        assertTrue(path.startsWith("documents/user1/"));
        assertTrue(path.endsWith("test.txt") || path.endsWith("-test.txt"));
    }

    @Test
    void testCalculateFileHash() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello world".getBytes());
        String hash = spacesService.calculateFileHash(file);
        assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", hash); // MD5 of "hello world"
    }
}
