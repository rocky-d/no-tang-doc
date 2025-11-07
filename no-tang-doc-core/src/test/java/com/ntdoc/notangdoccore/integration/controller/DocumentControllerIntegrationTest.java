//package com.ntdoc.notangdoccore.integration.controller;
//
//// DocumentController的集成测试
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nimbusds.oauth2.sdk.id.Actor;
//import com.ntdoc.notangdoccore.config.TestAsyncConfig;
//import com.ntdoc.notangdoccore.controller.DocumentController;
//import com.ntdoc.notangdoccore.dto.common.ApiResponse;
//import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
//import com.ntdoc.notangdoccore.entity.Log;
//import com.ntdoc.notangdoccore.entity.User;
//import com.ntdoc.notangdoccore.entity.logenum.ActorType;
//import com.ntdoc.notangdoccore.entity.logenum.OperationStatus;
//import com.ntdoc.notangdoccore.entity.logenum.OperationType;
//import com.ntdoc.notangdoccore.event.UserOperationEvent;
//import com.ntdoc.notangdoccore.listener.UserOperationLogListener;
//import com.ntdoc.notangdoccore.repository.LogRepository;
//import com.ntdoc.notangdoccore.service.DocumentService;
//
//import com.ntdoc.notangdoccore.service.FileStorageService;
//import com.ntdoc.notangdoccore.service.impl.UserSyncService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.system.OutputCaptureExtension;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(DocumentController.class)
//@AutoConfigureMockMvc
//@Slf4j
//@Import({TestAsyncConfig.class, UserOperationLogListener.class})
//@ActiveProfiles("test")
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//class DocumentControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private DocumentService documentService;
//    @MockitoBean
//    private LogRepository logRepository;
//    @MockitoBean
//    private UserSyncService userSyncService;
//    @MockitoBean
//    private FileStorageService fileStorageService;
//
//    @Autowired
//    private ApplicationEventPublisher eventPublisher;
//
//    @MockitoBean
//    private ClientRegistrationRepository clientRegistrationRepository;
//
//    @MockitoBean
//    private OAuth2AuthorizedClientManager authorizedClientManager;
//
//    @BeforeEach
//    void setUp() {
//        log.info("Test Begin");
//        reset(documentService,logRepository);
//        User u = new User();
//        u.setKcUserId("user-123");
//        u.setUsername("test_user");
//        when(userSyncService.ensureFromJwt(any())).thenReturn(u);
//
//    }
//
//    @AfterEach
//    void tearDown() {
//        log.info("Test End");
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("场景1: 上传成功-日志保存失败->Controller应该返回200成功")
//    void scenario1_UploadSuccess_LogFails_ShouldReturn200() throws Exception {
//        log.info("Scenario1: Upload Success But Log Fail");
//
//        //1. Mock 文档上传成功
//        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
//                .documentId(100L)
//                .fileName("test_file.pdf")
//                .fileSize(204800L)
//                .mimeType("application/pdf")
//                .s3Key("user-123/docs/test_file.pdf")
//                .uploadTime(LocalDateTime.now())
//                .userId("user-123")
//                .url("https://s3.example.com/user-123/docs/test_file.pdf")
//                .description("test file")
//                .build();
//        when(documentService.uploadDocument(any(MultipartFile.class),eq("test_file.pdf"),eq("test file"),eq("user-123"))).thenReturn(mockResponse);
//
//        AtomicBoolean calledFromListener = new AtomicBoolean(false);
//        AtomicReference<String> asyncThread = new AtomicReference<>();
//        CountDownLatch latch = new CountDownLatch(1);
//
//        //2. Mock 让日志保存抛出异常
//        doAnswer(inv -> {
//            System.out.println(">>> doAnswer triggered on thread " + Thread.currentThread().getName());
//
//            String threadName = Thread.currentThread().getName();
//            asyncThread.set(threadName);
//
//            boolean inListener = Arrays.stream(Thread.currentThread().getStackTrace())
//                    .anyMatch(el -> el.getMethodName().equals("handleUserOperation"));
//
//            calledFromListener.set(inListener);
//
//            latch.countDown();
//
//            throw new RuntimeException("simulate log save failed");
//        }).when(logRepository).save(any());
//
//        //3. 准备上传的测试文件
//        MockMultipartFile testFile = new MockMultipartFile(
//                "file",
//                "test_file.pdf",
//                MediaType.APPLICATION_PDF_VALUE,
//                "test content".getBytes()
//        );
//
//
//        // 6.记录请求开始时间
//        long startTime = System.currentTimeMillis();
//
//        // 7.Mock HTTP请求
//        MvcResult result = mockMvc.perform(
//                multipart("/api/v1/documents/upload")
//                        .file(testFile)
//                        .param("fileName","test_file.pdf")
//                        .param("description","test file")
//                        .with(jwt().jwt(builder -> builder.claim("sub","user-123").claim("preferred_username","test_user")
//                        ))
//        )
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("文件上传成功"))
//                .andExpect(jsonPath("$.data.documentId").value(100L))
//                .andExpect(jsonPath("$.data.fileName").value("test_file.pdf"))
//                .andReturn();
//
//        // 7. 记录响应时间
//        long responseTime = System.currentTimeMillis() - startTime;
//        log.info("Controller响应时间: {}ms",responseTime);
//
//        ApiResponse<DocumentUploadResponse> apiResponse = objectMapper.readValue(
//                result.getResponse().getContentAsString(),
//                new TypeReference<ApiResponse<DocumentUploadResponse>>() {}
//        );
//
//        assertTrue(apiResponse.isSuccess());
//        assertThat(apiResponse.getData().getDocumentId()).isEqualTo(100L);
//
//        assertTrue(latch.await(3, TimeUnit.SECONDS));
//
//        verify(logRepository,timeout(3000)).save(any());
//
//        assertTrue(calledFromListener.get(),"logRepository.save() 不是从 UserOperationListener调用的");
//
//        String asyncThreadName = asyncThread.get();
//        assertNotNull(asyncThreadName);
//        assertNotEquals(Thread.currentThread().getName(), asyncThreadName);
//
//        assertTrue(asyncThreadName.startsWith("Test-Async-"));
//
//    }
//
//
//    @Test
//    @Order(2)
//    @DisplayName("场景2: 上传成功-日志记录成功 -> Controller返回200且记录日志")
//    void scenario2_UploadSuccess_LogSuccess_ShouldReturn200_AndPersisting() throws Exception {
//        // 1.Mock 文件上传成功
//        DocumentUploadResponse mockResponse = DocumentUploadResponse.builder()
//                .documentId(100L)
//                .fileName("test_file.pdf")
//                .fileSize(1024L)
//                .mimeType("application/pdf")
//                .s3Key("user-123/docs/test_file.pdf")
//                .userId("user-123")
//                .url("https://s3.example.com/user-123/docs/test-file.pdf")
//                .description("ok")
//                .build();
//        when(documentService.uploadDocument(any(MultipartFile.class),anyString(),anyString(),anyString())).thenReturn(mockResponse);
//
//        // Mock LogRepository正常返回
//        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        //3. 准备上传的测试文件
//        MockMultipartFile testFile = new MockMultipartFile(
//                "file",
//                "test_file.pdf",
//                MediaType.APPLICATION_PDF_VALUE,
//                "test content".getBytes()
//        );
//
//
//        // 7.Mock HTTP请求
//        MvcResult result = mockMvc.perform(
//                        multipart("/api/v1/documents/upload")
//                                .file(testFile)
//                                .param("fileName","test_file.pdf")
//                                .param("description","test file")
//                                .with(jwt().jwt(builder -> builder.claim("sub","user-123").claim("preferred_username","test_user")
//                                ))
//                )
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("文件上传成功"))
//                .andExpect(jsonPath("$.data.documentId").value(100L))
//                .andExpect(jsonPath("$.data.fileName").value("test_file.pdf"))
//                .andReturn();
//
//        ApiResponse<DocumentUploadResponse> apiResponse = objectMapper.readValue(
//                result.getResponse().getContentAsString(),
//                new TypeReference<ApiResponse<DocumentUploadResponse>>() {}
//        );
//
//        assertThat(apiResponse.isSuccess()).isTrue();
//        assertThat(apiResponse.getData().getDocumentId()).isEqualTo(100L);
//
//        verify(logRepository,timeout(3000)).save(any());
//
//        ArgumentCaptor<Log> captor = ArgumentCaptor.forClass(Log.class);
//        verify(logRepository,atLeastOnce()).save(captor.capture());
//
//        Log saved = captor.getValue();
//        assertNotNull(saved);
//
//        assertThat(saved.getActorType()).isEqualTo(ActorType.USER);
//        assertThat(saved.getActorName()).isEqualTo("test_user");
//        assertThat(saved.getOperationType()).isEqualTo(OperationType.UPLOAD_DOCUMENT);
//        assertThat(saved.getTargetName()).isEqualTo("test_file.pdf");
//        assertThat(saved.getOperationStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(saved.getTime()).isNotNull();
//
//    }
//
//
//}
