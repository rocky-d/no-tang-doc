package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.dto.document.DocumentDownloadResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import com.ntdoc.notangdoccore.event.UserOperationEvent;
import com.ntdoc.notangdoccore.exception.DocumentException;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.FileStorageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URL;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DocumentServiceImpl服务测试")
public class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private DocumentServiceImpl documentService;

    private MockMultipartFile mockFile;
    private User mockUser;
    private Document mockDocument;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy".getBytes());
        mockUser = User.builder().id(1L).kcUserId("kc-123").username("testUser").email("test@example.com").build();
        mockDocument = Document.builder()
                .id(10L)
                .originalFilename("test.pdf")
                .s3Key("kc-123/test.pdf")
                .uploadedBy(mockUser)
                .fileSize(100L)
                .status(Document.DocumentStatus.ACTIVE)
                .contentType("application/pdf")
                .build();
    }

    // ---------------- uploadDocument -----------------

    @Test
    @Order(1)
    @DisplayName("测试1：上传文件 - 成功 - 文件保存与事件触发")
    void testUploadDocument_Success() throws Exception {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(fileStorageService.uploadFile(any(), any())).thenReturn("kc-123/test.pdf");
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> {
            Document d = i.getArgument(0);
            d.setId(99L);
            d.setUploadedBy(mockUser);
            d.setCreatedAt(Instant.now());
            return d;
        });

        DocumentUploadResponse response = documentService.uploadDocument(mockFile, null, "desc", "kc-123");

        assertThat(response).isNotNull();
        assertThat(response.getFileName()).isEqualTo("test.pdf");
        verify(eventPublisher, atLeastOnce()).publishEvent(any(UserOperationEvent.class));
    }

    @Test
    @Order(2)
    @DisplayName("测试2：上传文件 - 失败 - 文件为空抛异常")
    void testUploadDocument_Fail_EmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[0]);
        assertThatThrownBy(() -> documentService.uploadDocument(empty, null, "desc", "kc-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件不能为空");
    }

    @Test
    @Order(3)
    @DisplayName("测试3：上传文件 - 失败 - S3抛异常并触发失败事件")
    void testUploadDocument_Fail_S3Error() throws Exception {
        when(userRepository.findByKcUserId(any())).thenReturn(Optional.of(mockUser));
        when(fileStorageService.uploadFile(any(), any())).thenThrow(new RuntimeException("S3 Error"));

        assertThatThrownBy(() -> documentService.uploadDocument(mockFile, "abc.pdf", "desc", "kc-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("文件上传失败");
        verify(eventPublisher, atLeastOnce()).publishEvent(any(UserOperationEvent.class));
    }

    @Test
    @Order(4)
    @DisplayName("测试4：上传文件 - 失败 - 文件名过长")
    void testUploadDocument_Fail_FilenameTooLong() {
        String longName = "a".repeat(260) + ".pdf";
        MockMultipartFile longFile = new MockMultipartFile("file", longName, "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> documentService.uploadDocument(longFile, null, "desc", "kc-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件名无效或过长");
    }

    @Test
    @Order(5)
    @DisplayName("测试5：上传文件 - 失败 - 文件过大")
    void testUploadDocument_Fail_FileTooLarge() {
        byte[] largeBytes = new byte[101 * 1024 * 1024]; // 101MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "big.pdf", "application/pdf", largeBytes);

        assertThatThrownBy(() -> documentService.uploadDocument(largeFile, null, "desc", "kc-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件大小不能超过100MB");
    }

    @Test
    @Order(6)
    @DisplayName("测试6：上传文件 - 成功 - 哈希计算异常回退为时间戳")
    void testUploadDocument_Success_HashFallback() throws Exception {
        MockMultipartFile badFile = mock(MockMultipartFile.class);
        when(badFile.getOriginalFilename()).thenReturn("bad.pdf");
        when(badFile.getSize()).thenReturn(10L);
        when(badFile.getBytes()).thenThrow(new RuntimeException("hash fail"));

        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(fileStorageService.uploadFile(any(), any())).thenReturn("kc-123/bad.pdf");
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> {
            Document d = i.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(Instant.now());
            return d;
        });

        DocumentUploadResponse resp = documentService.uploadDocument(badFile, null, "desc", "kc-123");

        assertThat(resp).isNotNull();
        assertThat(resp.getFileName()).isEqualTo("bad.pdf");
        verify(eventPublisher, atLeastOnce()).publishEvent(any(UserOperationEvent.class));
    }


    // ---------------- getDocumentDownloadUrl -----------------

    @Test
    @Order(10)
    @DisplayName("测试10：下载文件 - 成功 - 校验权限并生成URL")
    void testGetDocumentDownloadUrl_Success() throws Exception {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(fileStorageService.generateDownloadUrl(anyString(), any())).thenReturn(new URL("https://mock-url.com"));
        when(documentRepository.save(any(Document.class))).thenReturn(mockDocument);

        DocumentDownloadResponse response = documentService.getDocumentDownloadUrl(10L, "kc-123");

        assertThat(response.getDownloadUrl()).contains("mock-url");
        verify(eventPublisher).publishEvent(any(UserOperationEvent.class));
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @Order(11)
    @DisplayName("测试11：下载文件 - 失败 - 无权访问")
    void testGetDocumentDownloadUrl_Fail_NoPermission() {
        User another = User.builder().id(2L).kcUserId("kc-999").build();
        mockDocument.setUploadedBy(another);

        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));

        assertThatThrownBy(() -> documentService.getDocumentDownloadUrl(10L, "kc-123"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("无权访问");
    }

    @Test
    @Order(12)
    @DisplayName("测试12：下载文件 - 失败 - 文档被删除")
    void testGetDocumentDownloadUrl_Fail_Deleted() {
        mockDocument.setStatus(Document.DocumentStatus.DELETED);
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));

        assertThatThrownBy(() -> documentService.getDocumentDownloadUrl(10L, "kc-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("文档已被删除");
    }

    @Test
    @Order(13)
    @DisplayName("测试13：下载文件 - 失败 - URL生成异常")
    void testGetDocumentDownloadUrl_Fail_GenerateUrlError() throws Exception {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(fileStorageService.generateDownloadUrl(anyString(), any()))
                .thenThrow(new RuntimeException("URL failed"));

        assertThatThrownBy(() -> documentService.getDocumentDownloadUrl(10L, "kc-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("生成下载链接失败");
        verify(eventPublisher, atLeastOnce()).publishEvent(any(UserOperationEvent.class));
    }



    // ---------------- deleteDocument -----------------

    @Test
    @Order(20)
    @DisplayName("测试20：删除文件 - 成功 - 仓库与S3删除均被调用")
    void testDeleteDocument_Success() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));

        documentService.deleteDocument(10L, "kc-123");

        verify(documentRepository).delete(mockDocument);
        verify(fileStorageService).deleteFile("kc-123/test.pdf");
        verify(eventPublisher, atLeastOnce()).publishEvent(any(UserOperationEvent.class));
    }

    @Test
    @Order(21)
    @DisplayName("测试21：删除文件 - 失败 - S3删除异常但不抛出Runtime")
    void testDeleteDocument_Fail_S3Error() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        doThrow(new RuntimeException("S3 delete failed")).when(fileStorageService).deleteFile(anyString());

        // 不应抛出异常，而是记录日志并发送事件
        documentService.deleteDocument(10L, "kc-123");

        verify(eventPublisher, atLeastOnce()).publishEvent(any(UserOperationEvent.class));
    }


    // ---------------- getUserDocuments -----------------

    @Test
    @Order(30)
    @DisplayName("测试30：获取用户文档列表 - 成功")
    void testGetUserDocuments_Success() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findByUploadedByOrderByCreatedAtDesc(mockUser))
                .thenReturn(List.of(mockDocument));

        List<Document> result = documentService.getUserDocuments("kc-123");
        assertThat(result).hasSize(1);
    }

    @Test
    @Order(31)
    @DisplayName("测试31：获取用户文档 - 按状态过滤")
    void testGetUserDocuments_ByStatus() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findByUploadedByAndStatusOrderByCreatedAtDesc(mockUser, Document.DocumentStatus.ACTIVE))
                .thenReturn(List.of(mockDocument));

        List<Document> result = documentService.getUserDocuments("kc-123", Document.DocumentStatus.ACTIVE);
        assertThat(result.get(0).getStatus()).isEqualTo(Document.DocumentStatus.ACTIVE);
    }

    // ---------------- getDocumentById -----------------

    @Test
    @Order(40)
    @DisplayName("测试40：根据ID获取文档 - 成功")
    void testGetDocumentById_Success() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));

        Document result = documentService.getDocumentById(10L, "kc-123");
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @Order(41)
    @DisplayName("测试41：根据ID获取文档 - 无权限")
    void testGetDocumentById_NoPermission() {
        User other = User.builder().id(9L).kcUserId("kc-999").build();
        mockDocument.setUploadedBy(other);

        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));

        assertThatThrownBy(() -> documentService.getDocumentById(10L, "kc-123"))
                .isInstanceOf(SecurityException.class);
    }

    // ---------------- incrementDownloadCount -----------------

    @Test
    @Order(50)
    @DisplayName("测试50：下载计数自增 - 成功")
    void testIncrementDownloadCount_Success() {
        mockDocument.setDownloadCount(5);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));

        documentService.incrementDownloadCount(10L);

        verify(documentRepository).save(mockDocument);
        assertThat(mockDocument.getDownloadCount()).isEqualTo(6);
    }

    // ---------------- searchDocumentsByFilename -----------------

    @Test
    @Order(60)
    @DisplayName("测试60：搜索文档 - 成功 - 模糊匹配")
    void testSearchDocumentsByFilename_Success() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findByUploadedByAndOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(any(), any()))
                .thenReturn(List.of(mockDocument));

        List<Document> result = documentService.searchDocumentsByFilename("kc-123", "test");
        assertThat(result).hasSize(1);
    }

    @Test
    @Order(61)
    @DisplayName("测试61：搜索文档 - 失败 - 关键字为空")
    void testSearchDocumentsByFilename_Fail_Empty() {
        assertThatThrownBy(() -> documentService.searchDocumentsByFilename("kc-123", " "))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("Search keyword must not be empty");
    }

    @Test
    @Order(62)
    @DisplayName("测试62：搜索文档 - 成功 - 忽略大小写")
    void testSearchDocumentsByFilename_Success_IgnoreCase() {
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(documentRepository.findByUploadedByAndOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(eq(mockUser), eq("TeSt")))
                .thenReturn(List.of(mockDocument));

        List<Document> result = documentService.searchDocumentsByFilename("kc-123", "TeSt");
        assertThat(result).hasSize(1);
    }

}
