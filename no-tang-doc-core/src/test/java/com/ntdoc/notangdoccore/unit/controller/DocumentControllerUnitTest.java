package com.ntdoc.notangdoccore.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntdoc.notangdoccore.controller.DocumentController;
import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.document.DeleteDocumentResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentDownloadResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.service.DocumentService;
import com.ntdoc.notangdoccore.service.DocumentTagService;
import com.ntdoc.notangdoccore.service.FileStorageService;
import com.ntdoc.notangdoccore.service.impl.UserSyncServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { DocumentController.class })
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DocumentController单元测试")
public class DocumentControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private UserSyncServiceImpl userSyncService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private DocumentTagService tagService;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        log.info("=== Test Begin ===");
        reset(documentService, userSyncService, fileStorageService);

        // 创建测试用户
        testUser = User.builder()
                .id(1L)
                .kcUserId("user-123")
                .username("test_user")
                .email("test@example.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        log.info("=== Test End ===\n");
    }

    // ==================== 辅助方法 ====================

    private Document createMockDocument(Long id, String filename, User user) {
        return Document.builder()
                .id(id)
                .originalFilename(filename)
                .storedFilename(filename)
                .fileSize(1024L)
                .contentType("application/pdf")
                .fileHash("abc123hash")
                .s3Bucket("test-bucket")
                .s3Key("user-123/docs/" + filename)
                .uploadedBy(user)
                .status(Document.DocumentStatus.ACTIVE)
                .description("test description")
                .downloadCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ==================== 上传文档测试 ====================

    @Test
    @Order(1)
    @DisplayName("测试1：上传文档 - 成功")
    void uploadDocument_Success() throws Exception {
        log.info("Test: Upload Document - Success");

        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
                .documentId(100L)
                .fileName("test_file.pdf")
                .fileSize(204800L)
                .mimeType("application/pdf")
                .s3Key("user-123/docs/test_file.pdf")
                .uploadTime(LocalDateTime.now())
                .userId("user-123")
                .url("https://s3.example.com/user-123/docs/test_file.pdf")
                .description("test file")
                .build();

        when(documentService.uploadDocument(
                any(MultipartFile.class),
                eq("test_file.pdf"),
                eq("test file"),
                eq("user-123")
        )).thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test content".getBytes()
        );

        MvcResult result = mockMvc.perform(
                        multipart("/api/v1/documents/upload")
                                .file(file)
                                .param("fileName", "test_file.pdf")
                                .param("description", "test file")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                        .claim("preferred_username", "test_user")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传成功"))
                .andExpect(jsonPath("$.data.documentId").value(100L))
                .andExpect(jsonPath("$.data.fileName").value("test_file.pdf"))
                .andExpect(jsonPath("$.data.fileSize").value(204800L))
                .andExpect(jsonPath("$.data.mimeType").value("application/pdf"))
                .andReturn();

        ApiResponse<DocumentUploadResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getDocumentId()).isEqualTo(100L);
        assertThat(response.getData().getFileName()).isEqualTo("test_file.pdf");

        verify(documentService, times(1)).uploadDocument(
                any(MultipartFile.class),
                eq("test_file.pdf"),
                eq("test file"),
                eq("user-123")
        );
    }

    @Test
    @Order(2)
    @DisplayName("测试2：上传文档 - 没有自定义文件名")
    void uploadDocument_WithoutCustomFileName() throws Exception {
        log.info("Test: Upload Document - Without Custom File Name");

        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
                .documentId(101L)
                .fileName("original.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .s3Key("user-123/docs/original.pdf")
                .userId("user-123")
                .build();

        when(documentService.uploadDocument(
                any(MultipartFile.class),
                isNull(),
                isNull(),
                eq("user-123")
        )).thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "original.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload")
                                .file(file)
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("original.pdf"));

        verify(documentService).uploadDocument(
                any(MultipartFile.class),
                isNull(),
                isNull(),
                eq("user-123")
        );
    }

    @Test
    @Order(3)
    @DisplayName("测试3：上传文档 - 参数错误(IllegalArgumentException)")
    void uploadDocument_InvalidArgument() throws Exception {
        log.info("Test: Upload Document - Invalid Argument");

        when(documentService.uploadDocument(
                any(MultipartFile.class),
                any(),
                any(),
                eq("user-123")
        )).thenThrow(new IllegalArgumentException("文件大小超过限制"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large_file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[1024 * 1024 * 100]
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload")
                                .file(file)
                                .param("fileName", "large_file.pdf")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求参数错误: 文件大小超过限制"));
    }

    @Test
    @Order(4)
    @DisplayName("测试4：上传文档 - 服务器错误(RuntimeException)")
    void uploadDocument_ServerError() throws Exception {
        log.info("Test: Upload Document - Server Error");

        when(documentService.uploadDocument(
                any(MultipartFile.class),
                anyString(),
                anyString(),
                eq("user-123")
        )).thenThrow(new RuntimeException("S3存储服务不可用"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload")
                                .file(file)
                                .param("fileName", "test.pdf")
                                .param("description", "test pdf")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("文件上传失败: S3存储服务不可用"));
    }

    @Test
    @Order(5)
    @DisplayName("测试5：上传文档 - 未认证(无JWT)")
    void uploadDocument_Unauthorized() throws Exception {
        log.info("Test: Upload Document - Unauthorized");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload")
                                .file(file)
                )
                .andExpect(status().isForbidden());

        verify(documentService, never()).uploadDocument(
                any(), any(), any(), any()
        );
    }

    // ==================== 获取下载链接测试 ====================

    @Test
    @Order(10)
    @DisplayName("测试10：获取下载链接 - 成功")
    void getDownloadUrl_Success() throws Exception {
        log.info("Test: Get Download URL - Success");

        Instant expirationTime = Instant.now().plusSeconds(600);

        DocumentDownloadResponse mockResponse = DocumentDownloadResponse.builder()
                .documentId(100L)
                .fileName("test.pdf")
                .downloadUrl("https://s3.example.com/presigned-url")
                .expiresAt(expirationTime)
                .fileSize(1024L)
                .mimeType("application/pdf")
                .build();

        when(documentService.getDocumentDownloadUrl(100L, "user-123"))
                .thenReturn(mockResponse);

        MvcResult result = mockMvc.perform(
                        get("/api/v1/documents/download/100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取下载链接成功"))
                .andExpect(jsonPath("$.data.documentId").value(100L))
                .andExpect(jsonPath("$.data.downloadUrl").value("https://s3.example.com/presigned-url"))
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andReturn();

        ApiResponse<DocumentDownloadResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getDocumentId()).isEqualTo(100L);
        assertThat(response.getData().getExpiresAt()).isNotNull();

        verify(documentService).getDocumentDownloadUrl(100L, "user-123");
    }

    @Test
    @Order(11)
    @DisplayName("测试11：获取下载链接 - 无权限(SecurityException)")
    void getDownloadUrl_Forbidden() throws Exception {
        log.info("Test: Get Download URL - Forbidden");

        when(documentService.getDocumentDownloadUrl(100L, "user-456"))
                .thenThrow(new SecurityException("无权访问该文档"));

        mockMvc.perform(
                        get("/api/v1/documents/download/100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-456")
                                ))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权访问该文档: 无权访问该文档"));
    }

    @Test
    @Order(12)
    @DisplayName("测试12：获取下载链接 - 文档不存在")
    void getDownloadUrl_NotFound() throws Exception {
        log.info("Test: Get Download URL - Not Found");

        when(documentService.getDocumentDownloadUrl(999L, "user-123"))
                .thenThrow(new RuntimeException("文档不存在"));

        mockMvc.perform(
                        get("/api/v1/documents/download/999")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 获取用户文档列表测试 ====================

    @Test
    @Order(20)
    @DisplayName("测试20：获取用户文档列表 - 所有文档")
    void getUserDocuments_AllDocuments() throws Exception {
        log.info("Test: Get User Documents - All");

        Document doc1 = createMockDocument(1L, "file1.pdf", testUser);
        Document doc2 = createMockDocument(2L, "file2.pdf", testUser);

        when(documentService.getUserDocuments("user-123"))
                .thenReturn(Arrays.asList(doc1, doc2));

        mockMvc.perform(
                        get("/api/v1/documents")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents").isArray())
                .andExpect(jsonPath("$.data.documents.length()").value(2))
                .andExpect(jsonPath("$.data.documents[0].documentId").value(1L))
                .andExpect(jsonPath("$.data.documents[1].documentId").value(2L));

        verify(documentService).getUserDocuments("user-123");
        verify(documentService, never()).getUserDocuments(anyString(), any());
    }

    @Test
    @Order(21)
    @DisplayName("测试21：获取用户文档列表 - 按状态过滤")
    void getUserDocuments_FilterByStatus() throws Exception {
        log.info("Test: Get User Documents - Filter By Status");

        Document doc1 = createMockDocument(1L, "active.pdf", testUser);

        when(documentService.getUserDocuments("user-123", Document.DocumentStatus.ACTIVE))
                .thenReturn(List.of(doc1));

        mockMvc.perform(
                        get("/api/v1/documents")
                                .param("status", "ACTIVE")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents").isArray())
                .andExpect(jsonPath("$.data.documents.length()").value(1));

        verify(documentService).getUserDocuments("user-123", Document.DocumentStatus.ACTIVE);
    }

    @Test
    @Order(22)
    @DisplayName("测试22：获取用户文档列表 - 空列表")
    void getUserDocuments_EmptyList() throws Exception {
        log.info("Test: Get User Documents - Empty List");

        when(documentService.getUserDocuments("user-123"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/documents")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents").isArray())
                .andExpect(jsonPath("$.data.documents.length()").value(0));
    }

    // ==================== 删除文档测试 ====================

    @Test
    @Order(30)
    @DisplayName("测试30：删除文档 - 成功")
    void deleteDocument_Success() throws Exception {
        log.info("Test: Delete Document - Success");

        Document mockDocument = createMockDocument(100L, "to_delete.pdf", testUser);

        when(documentService.getDocumentById(100L, "user-123"))
                .thenReturn(mockDocument);

        doNothing().when(documentService).deleteDocument(100L, "user-123");

        MvcResult result = mockMvc.perform(
                        delete("/api/v1/documents/100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("文档删除成功"))
                .andExpect(jsonPath("$.documentId").value(100L))
                .andExpect(jsonPath("$.fileName").value("to_delete.pdf"))
                .andExpect(jsonPath("$.permanent").value(false))
                .andReturn();

        DeleteDocumentResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                DeleteDocumentResponse.class
        );

        assertThat(response.getDocumentId()).isEqualTo(100L);
        assertThat(response.isPermanent()).isFalse();
        assertThat(response.getRecoveryDeadline()).isNotNull();

        verify(documentService).getDocumentById(100L, "user-123");
        verify(documentService).deleteDocument(100L, "user-123");
    }

    @Test
    @Order(31)
    @DisplayName("测试31：删除文档 - 文档不存在")
    void deleteDocument_NotFound() throws Exception {
        log.info("Test: Delete Document - Not Found");

        when(documentService.getDocumentById(999L, "user-123"))
                .thenThrow(new RuntimeException("文档不存在"));

        mockMvc.perform(
                        delete("/api/v1/documents/999")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError());

        verify(documentService).getDocumentById(999L, "user-123");
        verify(documentService, never()).deleteDocument(anyLong(), anyString());
    }

    // ==================== 生成分享链接测试 ====================

    @Test
    @Order(40)
    @DisplayName("测试40：生成分享链接 - 成功")
    void generateShareLink_Success() throws Exception {
        log.info("Test: Generate Share Link - Success");

        Document mockDocument = createMockDocument(100L, "share.pdf", testUser);

        when(documentService.getDocumentById(100L, "user-123"))
                .thenReturn(mockDocument);

        when(fileStorageService.fileExists(mockDocument.getS3Key()))
                .thenReturn(true);

        when(fileStorageService.generateShareUrl(
                eq(mockDocument.getS3Key()),
                eq(Duration.ofMinutes(10))
        )).thenReturn(new URL("https://s3.example.com/share-url"));

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "100")
                                .param("expirationMinutes", "10")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.url").value("https://s3.example.com/share-url"))
                .andExpect(jsonPath("$.documentId").value(100L))
                .andExpect(jsonPath("$.expirationMinutes").value(10));

        verify(documentService).getDocumentById(100L, "user-123");
        verify(fileStorageService).fileExists(mockDocument.getS3Key());
        verify(fileStorageService).generateShareUrl(
                eq(mockDocument.getS3Key()),
                eq(Duration.ofMinutes(10))
        );
    }

    @Test
    @Order(41)
    @DisplayName("测试41：生成分享链接 - 使用默认过期时间")
    void generateShareLink_DefaultExpiration() throws Exception {
        log.info("Test: Generate Share Link - Default Expiration");

        Document mockDocument = createMockDocument(100L, "share.pdf", testUser);

        when(documentService.getDocumentById(100L, "user-123"))
                .thenReturn(mockDocument);

        when(fileStorageService.fileExists(anyString())).thenReturn(true);

        when(fileStorageService.generateShareUrl(
                anyString(),
                any(Duration.class)
        )).thenReturn(new URL("https://s3.example.com/share-url"));

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationMinutes").value(10));
    }

    @Test
    @Order(42)
    @DisplayName("测试42：生成分享链接 - 过期时间无效")
    void generateShareLink_InvalidExpiration() throws Exception {
        log.info("Test: Generate Share Link - Invalid Expiration");

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "100")
                                .param("expirationMinutes", "0")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isBadRequest());

        verify(documentService, never()).getDocumentById(anyLong(), anyString());
    }

    @Test
    @Order(43)
    @DisplayName("测试43：生成分享链接 - 文档不存在")
    void generateShareLink_DocumentNotFound() throws Exception {
        log.info("Test: Generate Share Link - Document Not Found");

        when(documentService.getDocumentById(999L, "user-123"))
                .thenReturn(null);

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "999")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Document not fount in storage"));
    }

    @Test
    @Order(44)
    @DisplayName("测试44：生成分享链接 - S3文件不存在")
    void generateShareLink_S3FileNotFound() throws Exception {
        log.info("Test: Generate Share Link - S3 File Not Found");

        Document mockDocument = createMockDocument(100L, "missing.pdf", testUser);

        when(documentService.getDocumentById(100L, "user-123"))
                .thenReturn(mockDocument);

        when(fileStorageService.fileExists(mockDocument.getS3Key()))
                .thenReturn(false);

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Document not fount in storage"));

        verify(fileStorageService, never()).generateShareUrl(anyString(), any());
    }

    @Test
    @Order(45)
    @DisplayName("测试45：生成分享链接 - S3Key为空")
    void generateShareLink_EmptyS3Key() throws Exception {
        log.info("Test: Generate Share Link - Empty S3 Key");

        Document mockDocument = Document.builder()
                .id(100L)
                .s3Key("")
                .uploadedBy(testUser)
                .build();

        when(documentService.getDocumentById(100L, "user-123"))
                .thenReturn(mockDocument);

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isBadRequest());

        verify(fileStorageService, never()).fileExists(anyString());
    }

    @Test
    @Order(46)
    @DisplayName("测试46：生成分享链接 - 服务异常")
    void generateShareLink_ServiceException() throws Exception {
        log.info("Test: Generate Share Link - Service Exception");

        Document mockDocument = createMockDocument(100L, "error.pdf", testUser);

        when(documentService.getDocumentById(100L, "user-123"))
                .thenReturn(mockDocument);

        when(fileStorageService.fileExists(anyString()))
                .thenReturn(true);

        when(fileStorageService.generateShareUrl(anyString(), any()))
                .thenThrow(new RuntimeException("S3 service unavailable"));

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("generate share link failed"));
    }
    @Test
    @Order(47)
    @DisplayName("测试47：生成分享链接 - 其他异常")
    void generateShareLink_UnexpectedException() throws Exception {
        log.info("Test: Generate Share Link - Unexpected Exception");

        Document mockDocument = createMockDocument(123L, "broken.pdf", testUser);
        when(documentService.getDocumentById(123L, "user-123")).thenReturn(mockDocument);
        when(fileStorageService.fileExists(mockDocument.getS3Key())).thenReturn(true);
        when(fileStorageService.generateShareUrl(anyString(), any()))
                .thenThrow(new RuntimeException("unexpected error"));

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "123")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("generate share link failed"));
    }

    @Test
    @Order(48)
    @DisplayName("测试48：生成分享链接 - 无文档")
    void generateShareLink_DocumentIsNull() throws Exception {
        log.info("Test: Generate Share Link - Document Is Null");

        when(documentService.getDocumentById(300L, "user-123"))
                .thenReturn(null);

        mockMvc.perform(
                        get("/api/v1/documents/share")
                                .param("documentId", "300")
                                .param("expirationMinutes", "10")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Document not fount in storage"));

        verify(documentService).getDocumentById(300L, "user-123");
        verify(fileStorageService, never()).fileExists(any());
    }

    // Tag Function Test
    @Test
    @Order(50)
    @DisplayName("测试50：向文档添加Tag - 成功")
    void addTagToDocument_Success() throws Exception{
        log.info("Test: Add Tags to Document - Success");

        Document mockDocument = createMockDocument(100L, "tagged.pdf", testUser);

        when(tagService.addTags(eq(100L), anyList(), eq("user-123")))
                .thenReturn(mockDocument);

        mockMvc.perform(
                        post("/api/v1/documents/100/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of("AI", "Cloud")))
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("标签添加成功"));

        verify(tagService).addTags(eq(100L), anyList(), eq("user-123"));
    }

    @Test
    @Order(51)
    @DisplayName("测试51：向文档添加Tag - 失败")
    void addTagsToDocument_Failure() throws Exception {
        log.info("Test: Add Tags to Document - Failure");

        when(tagService.addTags(eq(100L), anyList(), eq("user-123")))
                .thenThrow(new RuntimeException("数据库写入失败"));

        mockMvc.perform(
                        post("/api/v1/documents/100/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of("AI", "Cloud")))
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("标签添加失败: 数据库写入失败"));

        verify(tagService).addTags(eq(100L), anyList(), eq("user-123"));
    }

    @Test
    @Order(60)
    @DisplayName("测试60：获得文档的Tag - 成功")
    void getTagsByDocument_Success() throws Exception {
        log.info("Test: Get Tags by Document - Success");

        com.ntdoc.notangdoccore.entity.Tag tag1 = com.ntdoc.notangdoccore.entity.Tag.builder().id(1L).tag("AI").build();
        com.ntdoc.notangdoccore.entity.Tag tag2 = com.ntdoc.notangdoccore.entity.Tag.builder().id(2L).tag("Cloud").build();

        when(tagService.getTags(100L))
                .thenReturn(List.of(tag1, tag2));

        mockMvc.perform(
                        get("/api/v1/documents/100/tags")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("AI"))
                .andExpect(jsonPath("$.data[1]").value("Cloud"));

        verify(tagService).getTags(100L);
    }

    @Test
    @Order(61)
    @DisplayName("测试61：获得文档的Tag - 无Tag")
    void getTagsByDocument_Empty() throws Exception {
        log.info("Test: Get Tags by Document - Empty");

        when(tagService.getTags(200L)).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/documents/200/tags")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(tagService).getTags(200L);
    }

    @Test
    @Order(70)
    @DisplayName("测试70：删除文档的Tag - 成功")
    void removeTagFromDocument_Success() throws Exception {
        log.info("Test: Remove Tag from Document - Success");

        Document mockDocument = createMockDocument(100L, "tagged.pdf", testUser);

        when(tagService.removeTag(100L, "AI", "user-123"))
                .thenReturn(mockDocument);

        mockMvc.perform(
                        delete("/api/v1/documents/100/tags/AI")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("标签删除成功"));

        verify(tagService).removeTag(100L, "AI", "user-123");
    }
    @Test
    @Order(71)
    @DisplayName("测试71：删除文档的Tag - 失败")
    void removeTagFromDocument_Failure() throws Exception {
        log.info("Test: Remove Tag from Document - Failure");

        when(tagService.removeTag(eq(100L), eq("AI"), eq("user-123")))
                .thenThrow(new RuntimeException("标签不存在"));

        mockMvc.perform(
                        delete("/api/v1/documents/100/tags/AI")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("标签删除失败: 标签不存在"));

        verify(tagService).removeTag(eq(100L), eq("AI"), eq("user-123"));
    }
    @Test
    @Order(80)
    @DisplayName("测试80：通过Tag分类文档 - 成功")
    void getDocumentsByTag_Success() throws Exception {
        log.info("Test: Get Documents by Tag - Success");

        Document doc1 = createMockDocument(1L, "AI1.pdf", testUser);
        Document doc2 = createMockDocument(2L, "AI2.pdf", testUser);

        when(tagService.getDocumentsByTag("AI"))
                .thenReturn(List.of(doc1, doc2));

        mockMvc.perform(
                        get("/api/v1/documents/by-tag/AI")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents[0].fileName").value("AI1.pdf"))
                .andExpect(jsonPath("$.data.documents[1].fileName").value("AI2.pdf"));

        verify(tagService).getDocumentsByTag("AI");
    }

    @Test
    @Order(81)
    @DisplayName("测试81：通过Tag分类文档 - 失败")
    void getDocumentsByTag_Failure() throws Exception {
        log.info("Test: Get Documents by Tag - Failure");

        when(tagService.getDocumentsByTag("AI"))
                .thenThrow(new RuntimeException("数据库查询失败"));

        mockMvc.perform(
                        get("/api/v1/documents/by-tag/AI")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Get documents by tag fail: 数据库查询失败"));

        verify(tagService).getDocumentsByTag("AI");
    }

    @Test
    @Order(90)
    @DisplayName("测试90：更新文档Tag - 成功")
    void uploadDocumentWithTags_Success() throws Exception {
        log.info("Test: Upload Document With Tags - Success");

        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
                .documentId(100L)
                .fileName("tagged_upload.pdf")
                .fileSize(2048L)
                .mimeType("application/pdf")
                .s3Key("user-123/docs/tagged_upload.pdf")
                .userId("user-123")
                .description("desc")
                .build();

        when(documentService.uploadDocument(
                any(),
                eq("tagged_upload.pdf"),
                eq("desc"),
                eq("user-123"))
        ).thenReturn(mockResponse);

        Document mockDocument = Document.builder()
                .id(100L)
                .originalFilename("tagged_upload.pdf")
                .description("desc")
                .status(Document.DocumentStatus.ACTIVE)
                .build();

        when(tagService.addTags(eq(100L), anyList(), eq("user-123")))
                .thenReturn(mockDocument);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tagged_upload.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload-with-tags")
                                .file(file)
                                .param("fileName", "tagged_upload.pdf")
                                .param("description", "desc")
                                .param("tags", "AI")
                                .param("tags", "ML")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传并添加标签成功"))
                .andExpect(jsonPath("$.data.documentId").value(100L))
                .andExpect(jsonPath("$.data.fileName").value("tagged_upload.pdf"));

        verify(documentService).uploadDocument(any(), eq("tagged_upload.pdf"), eq("desc"), eq("user-123"));
        verify(tagService).addTags(eq(100L), anyList(), eq("user-123"));
    }

    @Test
    @Order(91)
    @DisplayName("测试91：更新文档Tag - 失败")
    void uploadDocumentWithTags_TagFailure() throws Exception {
        log.info("Test: Upload Document With Tags - Tag Failure");

        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
                .documentId(200L)
                .fileName("error_upload.pdf")
                .fileSize(512L)
                .mimeType("application/pdf")
                .s3Key("user-123/docs/error_upload.pdf")
                .userId("user-123")
                .build();

        when(documentService.uploadDocument(any(), eq("error_upload.pdf"), eq("desc"), eq("user-123")))
                .thenReturn(mockResponse);

        doThrow(new RuntimeException("标签写入失败"))
                .when(tagService)
                .addTags(eq(200L), anyList(), eq("user-123"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "error_upload.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload-with-tags")
                                .file(file)
                                .param("fileName", "error_upload.pdf")
                                .param("description", "desc")
                                .param("tags", "AI")
                                .param("tags", "ML")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("文件上传或标签添加失败")));

        verify(documentService).uploadDocument(any(), eq("error_upload.pdf"), eq("desc"), eq("user-123"));
        verify(tagService).addTags(eq(200L), anyList(), eq("user-123"));
    }

    @Test
    @Order(92)
    @DisplayName("测试92：更新文档Tag - 无Tag")
    void uploadDocumentWithTags_NoTags() throws Exception {
        log.info("Test: Upload Document With Tags - No Tags");

        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
                .documentId(300L)
                .fileName("notag_upload.pdf")
                .fileSize(1234L)
                .mimeType("application/pdf")
                .s3Key("user-123/docs/notag_upload.pdf")
                .userId("user-123")
                .description("no tag file")
                .build();

        when(documentService.uploadDocument(any(), eq("notag_upload.pdf"), eq("no tag file"), eq("user-123")))
                .thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notag_upload.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload-with-tags")
                                .file(file)
                                .param("fileName", "notag_upload.pdf")
                                .param("description", "no tag file")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传并添加标签成功"))
                .andExpect(jsonPath("$.data.fileName").value("notag_upload.pdf"));

        verify(documentService).uploadDocument(any(), eq("notag_upload.pdf"), eq("no tag file"), eq("user-123"));
        verify(tagService, never()).addTags(anyLong(), anyList(), anyString());
    }

    @Test
    @Order(93)
    @DisplayName("测试93：更新文档Tag - 异常")
    void uploadDocumentWithTags_DocumentServiceThrowsException() throws Exception {
        log.info("Test: Upload Document With Tags - DocumentService Exception");

        when(documentService.uploadDocument(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("存储服务不可用"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "broken_upload.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "broken".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents/upload-with-tags")
                                .file(file)
                                .param("fileName", "broken_upload.pdf")
                                .param("description", "desc")
                                .param("tags", "AI")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("文件上传或标签添加失败: 存储服务不可用"));

        verify(documentService).uploadDocument(any(), any(), any(), any());
        verify(tagService, never()).addTags(anyLong(), anyList(), anyString());
    }

    @Test
    @Order(100)
    @DisplayName("测试100：通过文件名称查询文档 - 成功")
    void searchByFilename_Success() throws Exception {
        log.info("Test: Search By Filename - Success");

        Document doc1 = createMockDocument(1L, "ai_report.pdf", testUser);
        Document doc2 = createMockDocument(2L, "ai_paper.pdf", testUser);

        when(documentService.searchDocumentsByFilename("user-123", "ai"))
                .thenReturn(List.of(doc1, doc2));

        mockMvc.perform(
                        get("/api/v1/documents/search")
                                .param("keyword", "ai")
                                .with(jwt().jwt(builder -> builder.claim("sub", "user-123")))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documents").isArray())
                .andExpect(jsonPath("$.data.documents.length()").value(2))
                .andExpect(jsonPath("$.data.documents[0].fileName").value("ai_report.pdf"))
                .andExpect(jsonPath("$.data.documents[1].fileName").value("ai_paper.pdf"));

        verify(documentService).searchDocumentsByFilename("user-123", "ai");
    }
}
