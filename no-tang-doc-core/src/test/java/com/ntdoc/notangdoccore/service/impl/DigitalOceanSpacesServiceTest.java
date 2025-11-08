package com.ntdoc.notangdoccore.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DigitalOceanSpaces服务测试")
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

//  UploadFileTest
    @Test
    @Order(1)
    @DisplayName("测试1：上传文件 - 成功")
    void testUploadFile_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello world".getBytes());
        PutObjectResponse response = PutObjectResponse.builder().eTag("etag").build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(response);
        String key = spacesService.uploadFile(file, "user1");
        assertNotNull(key);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @Order(2)
    @DisplayName("测试2：上传文件 - 空文件")
    void testUploadFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> spacesService.uploadFile(file, "user1"));
    }

    @Test
    @Order(3)
    @DisplayName("测试3：上传文件 - IO异常")
    void testUploadFile_IOException() throws IOException {
        MultipartFile file = mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenThrow(new IOException("IO error"));
        assertThrows(RuntimeException.class, () -> spacesService.uploadFile(file, "user1"));
    }

    @Test
    @Order(4)
    @DisplayName("测试4：上传文件 - S3异常")
    void testUploadFile_S3Exception() {
        MockMultipartFile file = new MockMultipartFile("file", "fail.txt", "text/plain", "abc".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("upload failed").build());
        assertThrows(RuntimeException.class, () -> spacesService.uploadFile(file, "u1"));
    }

//  GenerateDownloadUrl
    @Test
    @Order(10)
    @DisplayName("测试10：生成下载URL - 成功")
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
    @Order(11)
    @DisplayName("测试11：生成下载URL - 失败")
    void testGenerateDownloadUrl_Fail() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("presign fail"));
        assertThrows(RuntimeException.class, () -> spacesService.generateDownloadUrl("a/b.txt", Duration.ofSeconds(10)));
    }

//  fileExists
    @Test
    @Order(20)
    @DisplayName("测试20：文件是否存在 - 存在")
    void testFileExists_True() {
        String key = "documents/user1/2025/10/test.txt";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
        assertTrue(spacesService.fileExists(key));
    }

    @Test
    @Order(21)
    @DisplayName("测试21：文件是否存在 - 不存在")
    void testFileExists_False() {
        String key = "documents/user1/2025/10/test.txt";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertFalse(spacesService.fileExists(key));
    }

    @Test
    @Order(22)
    @DisplayName("测试22：文件是否存在 - 异常")
    void testFileExists_Exception() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("network").build());
        assertFalse(spacesService.fileExists("any.txt"));
    }

//  deleteFile
    @Test
    @Order(30)
    @DisplayName("测试30：删除文件 - 成功")
    void testDeleteFile_Success() {
        String key = "documents/user1/2025/10/test.txt";
        DeleteObjectResponse response = DeleteObjectResponse.builder().build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(response);
        assertTrue(spacesService.deleteFile(key));
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @Order(31)
    @DisplayName("测试31：删除文件 - 异常")
    void testDeleteFile_Exception() {
        String key = "documents/user1/2025/10/test.txt";
        doThrow(S3Exception.builder().build()).when(s3Client).deleteObject(any(DeleteObjectRequest.class));
        assertFalse(spacesService.deleteFile(key));
    }

//  generateStoragePath
    @Test
    @Order(40)
    @DisplayName("测试40：生成文件存储路径")
    void testGenerateStoragePath() {
        String path = spacesService.generateStoragePath("user1", "test.txt");
        assertTrue(path.startsWith("documents/user1/"));
        assertTrue(path.endsWith("test.txt") || path.endsWith("-test.txt"));
    }

//  calculateFileHash
    @Test
    @Order(50)
    @DisplayName("测试50：生成文件哈希值")
    void testCalculateFileHash() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello world".getBytes());
        String hash = spacesService.calculateFileHash(file);
        assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", hash); // MD5 of "hello world"
    }

    @Test
    @Order(51)
    @DisplayName("测试51：生成文件哈希值 - 异常")
    void testCalculateFileHash_Exception() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("err.txt");
        when(file.getBytes()).thenThrow(new IOException("read error"));
        assertThrows(RuntimeException.class, () -> spacesService.calculateFileHash(file));
    }

//  generateShareUrl
    @Test
    @Order(60)
    @DisplayName("测试60：生成文件分享链接 - 成功")
    void testGenerateShareUrl_Success() {
        URL url = mock(URL.class);
        software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presigned = mock(software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(url);
        when(s3Presigner.presignGetObject(any(software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.class))).thenReturn(presigned);
        URL result = spacesService.generateShareUrl("documents/u1/test.pdf", Duration.ofMinutes(5));
        assertEquals(url, result);
    }

    @Test
    @Order(61)
    @DisplayName("测试61：生成文件分享链接 - 异常")
    void testGenerateShareUrl_Exception() {
        when(s3Presigner.presignGetObject(any(software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("share fail"));
        assertThrows(RuntimeException.class, () -> spacesService.generateShareUrl("doc/u1.pdf", Duration.ofMinutes(1)));
    }

//  generateUploadUrl
    @Test
    @Order(70)
    @DisplayName("测试70：生成文件上传链接 - 成功")
    void testGenerateUploadUrl_Success() {
        URL url = mock(URL.class);
        software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest presigned = mock(software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest.class);
        when(presigned.url()).thenReturn(url);
        when(s3Presigner.presignPutObject(any(software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.class))).thenReturn(presigned);
        URL result = spacesService.generateUploadUrl("u1/file.txt", "text/plain", Duration.ofMinutes(3));
        assertEquals(url, result);
    }

    @Test
    @Order(71)
    @DisplayName("测试71：生成文件上传链接 - 异常")
    void testGenerateUploadUrl_Exception() {
        when(s3Presigner.presignPutObject(any(software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("upload url fail"));
        assertThrows(RuntimeException.class, () -> spacesService.generateUploadUrl("u1/file.txt", "text/plain", Duration.ofMinutes(3)));
    }

//  sanitizeFilename
    @Test
    @Order(80)
    @DisplayName("测试80：整理文件名称")
    void testSanitizeFilename() throws Exception {
        java.lang.reflect.Method method = DigitalOceanSpacesService.class.getDeclaredMethod("sanitizeFilename", String.class);
        method.setAccessible(true);

        String cleaned1 = (String) method.invoke(spacesService, "  test file?.txt  ");
        assertEquals("test-file-.txt", cleaned1);

        String cleaned2 = (String) method.invoke(spacesService, " /\\:*?\"<>|  ");
        assertEquals("file", cleaned2);

        String cleaned3 = (String) method.invoke(spacesService, "");
        assertEquals("unnamed-file", cleaned3);

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 120; i++) longName.append("a");
        String cleaned4 = (String) method.invoke(spacesService, longName.toString());
        assertTrue(cleaned4.length() <= 100);
    }

}
