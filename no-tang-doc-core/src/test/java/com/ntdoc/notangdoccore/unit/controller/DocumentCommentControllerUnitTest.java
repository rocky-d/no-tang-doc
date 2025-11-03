package com.ntdoc.notangdoccore.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntdoc.notangdoccore.controller.DocumentCommentController;
import com.ntdoc.notangdoccore.dto.comment.*;
import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.entity.*;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.service.DocumentCommentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DocumentCommentController 单元测试（使用 @MockitoBean 而非 @MockBean）
 */
@WebMvcTest(controllers = {DocumentCommentController.class})
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DocumentCommentController单元测试")
public class DocumentCommentControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentCommentService commentService;
    @MockitoBean
    private DocumentRepository documentRepository;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        log.info("=== Test Begin ===");
        reset(commentService, documentRepository);

        testDocument = Document.builder()
                .id(1L)
                .originalFilename("test.pdf")
                .createdAt(Instant.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        log.info("=== Test End ===\n");
    }

    // ========== Create Comment Tests ==========

    @Test
    @Order(1)
    @DisplayName("测试1：创建评论 - 成功")
    void createComment_Success() throws Exception {
        //构造请求体
        DocumentCommentCreateRequest req = new DocumentCommentCreateRequest();
        req.setDocumentId(1L);     // 必填字段
        req.setTeamId(10L);
        req.setContent("Nice work!");

        User mockUser = User.builder().id(1L).username("testUser").build();
        Document mockDocument = Document.builder().id(1L).build();

        DocumentComment mockComment = DocumentComment.builder()
                .id(100L)
                .content("Nice work!")
                .status(DocumentComment.CommentStatus.ACTIVE)
                .user(mockUser)
                .document(mockDocument)
                .build();

        // Mock service 行为
        when(commentService.createComment(eq(1L), eq(10L), eq("Nice work!"), isNull(), eq("kc-123")))
                .thenReturn(mockComment);

        // 发起请求（带 JWT 模拟登录用户）
        mockMvc.perform(post("/api/v1/documents/1/comments")
                        .with(jwt().jwt(builder -> builder.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.content").value("Nice work!"));

        // 验证 service 调用
        verify(commentService).createComment(eq(1L), eq(10L), eq("Nice work!"), isNull(), eq("kc-123"));
    }


    @Test
    @Order(2)
    @DisplayName("测试2：创建评论 - 权限拒绝(SecurityException)")
    void createComment_Forbidden() throws Exception {
        DocumentCommentCreateRequest req = new DocumentCommentCreateRequest();
        req.setDocumentId(1L);
        req.setTeamId(10L);
        req.setContent("No permission");

        when(commentService.createComment(anyLong(), any(), any(), any(), any()))
                .thenThrow(new SecurityException("您不是该团队成员，无法评论"));

        mockMvc.perform(post("/api/v1/documents/1/comments")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden())                           // HTTP 403
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("您不是该团队成员，无法评论"));

        verify(commentService).createComment(eq(1L), eq(10L), eq("No permission"), isNull(), eq("kc-123"));
    }


    @Test
    @Order(3)
    @DisplayName("测试3：创建评论 - 触发 @Valid 校验失败 (MethodArgumentNotValidException)")
    void createComment_ValidationFailed() throws Exception {
        DocumentCommentCreateRequest req = new DocumentCommentCreateRequest();
        req.setDocumentId(1L);
        req.setTeamId(10L);
        req.setContent("");

        // 不 mock service —— 让请求真正进入 @Valid 校验阶段
        // 如果提前 stub 了 commentService.createComment()，校验阶段就会被跳过

        mockMvc.perform(post("/api/v1/documents/1/comments")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                // 校验触发的异常类型确实是 MethodArgumentNotValidException
                .andExpect(result ->
                        assertTrue(
                                result.getResolvedException() instanceof MethodArgumentNotValidException,
                                "应当触发 @Valid 注解导致的 MethodArgumentNotValidException"
                        )
                );
    }


    @Test
    @Order(4)
    @DisplayName("测试4：创建评论 - 服务器异常(RuntimeException)")
    void createComment_InternalError() throws Exception {
        DocumentCommentCreateRequest req = new DocumentCommentCreateRequest();
        req.setDocumentId(1L);
        req.setTeamId(10L);
        req.setContent("error");  // 模拟会触发 service 异常的输入
        req.setParentCommentId(null);

        when(commentService.createComment(anyLong(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("数据库写入失败"));

        mockMvc.perform(post("/api/v1/documents/1/comments")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("创建评论失败")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("数据库写入失败")));

        verify(commentService).createComment(eq(1L), eq(10L), eq("error"), isNull(), eq("kc-123"));
    }


    // ========== Get Comments ==========

    @Test
    @Order(10)
    @DisplayName("测试10：获取文档评论 - 成功")
    void getComments_Success() throws Exception {
        User testUser = User.builder().id(1L).username("tester").build();

        Document testDocument = Document.builder()
                .id(1L)
                .originalFilename("test.txt")
                .storedFilename("stored_123.txt")
                .fileSize(1234L)
                .contentType("text/plain")
                .fileHash("abcd1234")
                .s3Bucket("ntdoc-bucket")
                .s3Key("kc-123/test.txt")
                .uploadedBy(testUser)
                .status(Document.DocumentStatus.ACTIVE)
                .build();

        DocumentComment comment = DocumentComment.builder()
                .id(1L)
                .content("Great!")
                .document(testDocument)
                .user(testUser)
                .status(DocumentComment.CommentStatus.ACTIVE)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(commentService.getDocumentComments(eq(1L), isNull(), eq("kc-123")))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/api/v1/documents/1/comments")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalComments").value(1))
                .andExpect(jsonPath("$.data.documentId").value(1))
                .andExpect(jsonPath("$.data.comments[0].content").value("Great!"));

        verify(commentService).getDocumentComments(eq(1L), isNull(), eq("kc-123"));
    }

    @Test
    @Order(11)
    @DisplayName("测试11：获取文档评论 - 权限拒绝(SecurityException)")
    void getComments_Forbidden() throws Exception {
        when(commentService.getDocumentComments(anyLong(), any(), any()))
                .thenThrow(new SecurityException("无权查看"));

        mockMvc.perform(get("/api/v1/documents/1/comments")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权查看"));

        verify(commentService).getDocumentComments(eq(1L), isNull(), eq("kc-123"));
    }


    @Test
    @Order(12)
    @DisplayName("测试12：获取文档评论 - 服务器异常")
    void getComments_InternalError() throws Exception {
        when(commentService.getDocumentComments(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/api/v1/documents/1/comments")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("获取评论列表失败")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("DB Error")));

        verify(commentService).getDocumentComments(eq(1L), isNull(), eq("kc-123"));
    }



    // ========== Update Comment ==========

    @Test
    @Order(20)
    @DisplayName("测试20：更新评论 - 成功")
    void updateComment_Success() throws Exception {
        DocumentCommentUpdateRequest req = new DocumentCommentUpdateRequest();
        req.setContent("Updated content");

        Document mockDocument = Document.builder().id(1L).build();
        User mockUser = User.builder().id(1L).username("tester").build();

        DocumentComment updated = DocumentComment.builder()
                .id(10L)
                .content("Updated content")
                .document(mockDocument)
                .user(mockUser)
                .status(DocumentComment.CommentStatus.ACTIVE)
                .build();

        when(commentService.updateComment(eq(10L), eq("Updated content"), eq("kc-123")))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/documents/1/comments/10")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("评论更新成功"))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.content").value("Updated content"));

        verify(commentService).updateComment(eq(10L), eq("Updated content"), eq("kc-123"));
    }


    @Test
    @Order(21)
    @DisplayName("测试21：更新评论 - 权限拒绝")
    void updateComment_Forbidden() throws Exception {
        DocumentCommentUpdateRequest req = new DocumentCommentUpdateRequest();
        req.setContent("test");

        when(commentService.updateComment(anyLong(), anyString(), anyString()))
                .thenThrow(new SecurityException("无权限编辑"));

        mockMvc.perform(put("/api/v1/documents/1/comments/5")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限编辑"));

        verify(commentService).updateComment(eq(5L), eq("test"), eq("kc-123"));
    }

    @Test
    @Order(22)
    @DisplayName("测试22：更新评论 - 参数错误")
    void updateComment_BadRequest() throws Exception {
        DocumentCommentUpdateRequest req = new DocumentCommentUpdateRequest();
        req.setContent("error");

        when(commentService.updateComment(anyLong(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("无效评论"));

        mockMvc.perform(put("/api/v1/documents/1/comments/5")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无效评论"));

        verify(commentService).updateComment(eq(5L), eq("error"), eq("kc-123"));
    }


    // ========== Delete Comment ==========

    @Test
    @Order(30)
    @DisplayName("测试30：删除评论 - 成功")
    void deleteComment_Success() throws Exception {
        doNothing().when(commentService).deleteComment(10L, "kc-123");

        mockMvc.perform(delete("/api/v1/documents/1/comments/10")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("评论删除成功"));

        verify(commentService).deleteComment(eq(10L), eq("kc-123"));
    }

    @Test
    @Order(31)
    @DisplayName("测试31：删除评论 - 权限拒绝")
    void deleteComment_Forbidden() throws Exception {
        doThrow(new SecurityException("无权删除")).when(commentService).deleteComment(10L, "kc-123");

        mockMvc.perform(delete("/api/v1/documents/1/comments/10")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权删除"));

        verify(commentService).deleteComment(eq(10L), eq("kc-123"));
    }

    @Test
    @Order(32)
    @DisplayName("测试32：删除评论 - 系统异常")
    void deleteComment_InternalError() throws Exception {
        doThrow(new RuntimeException("DB崩溃")).when(commentService).deleteComment(10L, "kc-123");

        mockMvc.perform(delete("/api/v1/documents/1/comments/10")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("删除评论失败")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("DB崩溃")));

        verify(commentService).deleteComment(eq(10L), eq("kc-123"));
    }


    // ========== Get Replies ==========

    @Test
    @Order(40)
    @DisplayName("测试40：获取评论回复 - 成功")
    void getCommentReplies_Success() throws Exception {
        Document mockDocument = Document.builder().id(1L).build();
        User mockUser = User.builder().id(1L).username("tester").build();

        DocumentComment reply = DocumentComment.builder()
                .id(5L)
                .content("Reply")
                .document(mockDocument)
                .user(mockUser)
                .status(DocumentComment.CommentStatus.ACTIVE)
                .build();

        when(commentService.getCommentReplies(eq(10L)))
                .thenReturn(List.of(reply));

        mockMvc.perform(get("/api/v1/documents/1/comments/10/replies")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取回复列表成功"))
                .andExpect(jsonPath("$.data[0].id").value(5L))
                .andExpect(jsonPath("$.data[0].content").value("Reply"));


        verify(commentService).getCommentReplies(eq(10L));
    }



    @Test
    @Order(41)
    @DisplayName("测试41：获取评论回复 - 系统异常")
    void getCommentReplies_InternalError() throws Exception {
        when(commentService.getCommentReplies(anyLong()))
                .thenThrow(new RuntimeException("数据库错误"));

        mockMvc.perform(get("/api/v1/documents/1/comments/10/replies")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "kc-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("获取回复列表失败")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("数据库错误")));

        verify(commentService).getCommentReplies(eq(10L));
    }

}
