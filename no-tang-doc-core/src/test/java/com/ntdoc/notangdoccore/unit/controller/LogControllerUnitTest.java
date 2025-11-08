package com.ntdoc.notangdoccore.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntdoc.notangdoccore.controller.LogController;
import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationStatus;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import com.ntdoc.notangdoccore.service.impl.UserSyncServiceImpl;
import com.ntdoc.notangdoccore.service.impl.LogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// LogController的单元测试
@WebMvcTest(controllers = LogController.class,
        excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("LogController单元测试")
public class LogControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LogServiceImpl logService;

    @MockitoBean
    private UserSyncServiceImpl userSyncService;

    private User mockUser;
    private Log mockLog;
    private Log mockLog2;
    private List<Log> mockLogs;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(100L);
        mockUser.setUsername("mockUser");

        mockLog = new Log();
        mockLog.setId(1L);
        mockLog.setActorType(ActorType.USER);
        mockLog.setActorName("mockUser");
        mockLog.setUserId(100L);
        mockLog.setOperationType(OperationType.UPLOAD_DOCUMENT);
        mockLog.setTargetName("test.pdf");
        mockLog.setOperationStatus(OperationStatus.SUCCESS);
        mockLog.setMessage("Upload Successful");
        mockLog.setTime(Instant.now());

        mockLog2 = new Log();
        mockLog2.setId(2L);
        mockLog2.setActorType(ActorType.USER);
        mockLog2.setActorName("mockUser");
        mockLog2.setUserId(100L);
        mockLog2.setOperationType(OperationType.DELETE_DOCUMENT);
        mockLog2.setTargetName("old.pdf");
        mockLog2.setOperationStatus(OperationStatus.SUCCESS);
        mockLog2.setMessage("Delete Successful");
        mockLog2.setTime(Instant.now());

        mockLogs = Arrays.asList(mockLog,mockLog2);
    }


    // 测试 GET /api/logs/list 找到
    @Test
    @Order(1)
    @DisplayName("测试1：获取用户的所有日志 - 成功")
    void testGetLogs_Found() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getAllLogsByUserId(100L)).thenReturn(mockLogs);

        mockMvc.perform(get("/api/v1/logs/list")
                .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].actorName", is("mockUser")))
                .andExpect(jsonPath("$[0].userId", is(100)))
                .andExpect(jsonPath("$[0].operationType", is("UPLOAD_DOCUMENT")))
                .andExpect(jsonPath("$[0].operationStatus", is("SUCCESS")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].operationType", is("DELETE_DOCUMENT")));

        verify(userSyncService,times(1)).ensureFromJwt(any());
        verify(logService,times(1)).getAllLogsByUserId(100L);
    }

    //测试 GET /api/logs/list 未找到
    @Test
    @Order(2)
    @DisplayName("测试2：获取用户的所有日志 - 未找到{空列表}")
    void testGetLogs_NotFound() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getAllLogsByUserId(100L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/logs/list").with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$",hasSize(0)));

        verify(userSyncService,times(1)).ensureFromJwt(any());
        verify(logService,times(1)).getAllLogsByUserId(100L);
    }

    //测试 GET /api/logs/list 用户认证失败
    @Test
    @Order(3)
    @DisplayName("测试3：获取用户的所有日志 - 用户认证失败")
    void testGetLogs_UserAuthFailed() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenThrow(new RuntimeException("User Auth Failed"));

        mockMvc.perform(get("/api/v1/logs/list").with(jwt()))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(userSyncService,times(1)).ensureFromJwt(any());
        verify(logService,never()).getAllLogsByUserId(anyLong());
    }

    @Test
    @Order(4)
    @DisplayName("测试4：获取用户的所有日志 - 服务异常导致500返回")
    void testGetLogs_ServiceException() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getAllLogsByUserId(anyLong())).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/api/v1/logs/list").with(jwt()))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(userSyncService, times(1)).ensureFromJwt(any());
        verify(logService, times(1)).getAllLogsByUserId(100L);
    }

    @Test
    @Order(5)
    @DisplayName("测试5：获取用户的所有日志 - 空列表但成功触发log.info")
    void testGetLogs_EmptyButSuccess() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getAllLogsByUserId(100L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/logs/list").with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(logService, times(1)).getAllLogsByUserId(100L);
    }

    //测试获取周日志-成功
    @Test
    @Order(10)
    @DisplayName("测试10：获取周日志统计 - 成功")
    void testGetLogsCount_Week_Success() throws Exception {
        Map<String,Long> weeklyCount = new HashMap<>();
        weeklyCount.put("2025-10-09", 3L);
        weeklyCount.put("2025-10-10", 5L);
        weeklyCount.put("2025-10-11", 2L);
        weeklyCount.put("2025-10-12", 1L);
        weeklyCount.put("2025-10-13", 4L);
        weeklyCount.put("2025-10-14", 6L);
        weeklyCount.put("2025-10-15", 7L);

        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(100L,"week")).thenReturn(weeklyCount);

        mockMvc.perform(get("/api/v1/logs/count")
                        .param("period","week").with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$['2025-10-09']", is(3)))
                .andExpect(jsonPath("$['2025-10-10']", is(5)))
                .andExpect(jsonPath("$['2025-10-11']", is(2)))
                .andExpect(jsonPath("$['2025-10-12']", is(1)))
                .andExpect(jsonPath("$['2025-10-13']", is(4)))
                .andExpect(jsonPath("$['2025-10-14']", is(6)))
                .andExpect(jsonPath("$['2025-10-15']", is(7)));

        verify(userSyncService,times(1)).ensureFromJwt(any());
        verify(logService,times(1)).getLogsCountByUser(100L,"week");
    }

    @Test
    @Order(11)
    @DisplayName("测试11：获取周日志统计 - 部分日期有数据")
    void testGetLogsCount_Week_PartialData() throws Exception {
        // Given - 只有部分日期有日志
        Map<String, Long> weeklyCount = new LinkedHashMap<>();
        weeklyCount.put("2025-10-13", 8L);
        weeklyCount.put("2025-10-15", 5L);

        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(100L, "week")).thenReturn(weeklyCount);

        // When & Then
        mockMvc.perform(get("/api/v1/logs/count")
                        .param("period", "week")
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$['2025-10-13']", is(8)))
                .andExpect(jsonPath("$['2025-10-15']", is(5)))
                .andExpect(jsonPath("$", aMapWithSize(2)));

        verify(logService, times(1)).getLogsCountByUser(100L, "week");
    }

    @Test
    @Order(12)
    @DisplayName("测试12：获取周日志统计 - 无数据")
    void testGetLogsCount_Week_NoData() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(100L,"week")).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/v1/logs/count")
                .param("period","week")
                .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$",anEmptyMap()));

        verify(logService,times(1)).getLogsCountByUser(100L,"week");
    }

    @Test
    @Order(13)
    @DisplayName("测试13：获取月日志统计 - 成功（按周统计过去4周）")
    void testGetLogsCount_Month_Success() throws Exception {
        // Given - 模拟过去4周的日志统计，key是 Week + 周数
        Map<String, Long> monthlyCount = new LinkedHashMap<>();
        monthlyCount.put("W202538", 15L);  // 9月第3周
        monthlyCount.put("W202539", 20L);  // 9月第4周
        monthlyCount.put("W202540", 18L);  // 10月第1周
        monthlyCount.put("W202541", 22L);  // 10月第2周

        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(100L, "month")).thenReturn(monthlyCount);

        // When & Then
        mockMvc.perform(get("/api/v1/logs/count")
                        .param("period", "month")
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.W202538", is(15)))
                .andExpect(jsonPath("$.W202539", is(20)))
                .andExpect(jsonPath("$.W202540", is(18)))
                .andExpect(jsonPath("$.W202541", is(22)))
                .andExpect(jsonPath("$", aMapWithSize(4)));

        verify(userSyncService, times(1)).ensureFromJwt(any());
        verify(logService, times(1)).getLogsCountByUser(100L, "month");
    }

    @Test
    @Order(14)
    @DisplayName("测试14：获取月日志统计 - 部分周有数据")
    void testGetLogsCount_Month_PartialData() throws Exception {
        // Given - 只有2周有数据
        Map<String, Long> monthlyCount = new LinkedHashMap<>();
        monthlyCount.put("W202540", 25L);
        monthlyCount.put("W202541", 30L);

        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(100L, "month")).thenReturn(monthlyCount);

        // When & Then
        mockMvc.perform(get("/api/v1/logs/count")
                        .param("period", "month")
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.W202540", is(25)))
                .andExpect(jsonPath("$.W202541", is(30)))
                .andExpect(jsonPath("$", aMapWithSize(2)));

        verify(logService, times(1)).getLogsCountByUser(100L, "month");
    }

    @Test
    @Order(15)
    @DisplayName("测试15：获取月日志统计 - 无数据")
    void testGetLogsCount_Month_NoData() throws Exception {
        // Given
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(100L, "month")).thenReturn(Collections.emptyMap());

        // When & Then
        mockMvc.perform(get("/api/v1/logs/count")
                        .param("period", "month")
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", anEmptyMap()));

        verify(logService, times(1)).getLogsCountByUser(100L, "month");
    }

    @Test
    @Order(16)
    @DisplayName("测试16：获取日志统计 - 无效period参数")
    void testGetLogsCount_InvalidPeriod() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);

        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get("/api/v1/logs/count")
                                .param("period", "year"))
                        .andReturn() // 触发执行
        );

        // 断言根因是参数校验异常（取根因链）
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof ConstraintViolationException)) {
            cause = cause.getCause();
        }
        assertTrue(cause instanceof ConstraintViolationException,
                "root cause should be ConstraintViolationException");

        // 未进入 service
        verify(logService, never()).getLogsCountByUser(anyLong(), anyString());
    }

    @Test
    @Order(17)
    @DisplayName("测试17：获取日志统计 - 服务异常导致400返回")
    void testGetLogsCount_ServiceException() throws Exception {
        when(userSyncService.ensureFromJwt(any())).thenReturn(mockUser);
        when(logService.getLogsCountByUser(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Query failed"));

        mockMvc.perform(get("/api/v1/logs/count")
                        .param("period", "week")
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(logService, times(1)).getLogsCountByUser(100L, "week");
    }


    @Test
    @Order(20)
    @DisplayName("测试20：获取文档操作日志 - 成功")
    void testListDocumentsLog_Success() throws Exception {
        // Given
        Long documentId = 500L;
        Log docLog = new Log();
        docLog.setId(10L);
        docLog.setTargetId(documentId);
        docLog.setOperationType(OperationType.UPDATE_DOCUMENT);
        docLog.setOperationStatus(OperationStatus.SUCCESS);

        List<Log> documentLogs = Arrays.asList(docLog);

        when(logService.getAllLogsByTargetId(documentId)).thenReturn(documentLogs);

        // When & Then
        mockMvc.perform(get("/api/v1/logs/documents")
                        .param("documentId", String.valueOf(documentId))
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].targetId", is(500)))
                .andExpect(jsonPath("$[0].operationType", is("UPDATE_DOCUMENT")));

        verify(logService, times(1)).getAllLogsByTargetId(documentId);
    }

    @Test
    @Order(21)
    @DisplayName("测试21：获取文档操作日志 - 未找到")
    void testListDocumentsLog_NotFound() throws Exception {
        // Given
        Long documentId = 999L;
        when(logService.getAllLogsByTargetId(documentId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/logs/documents")
                        .param("documentId", String.valueOf(documentId))
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(logService, times(1)).getAllLogsByTargetId(documentId);
    }

    @Test
    @Order(22)
    @DisplayName("测试22：获取文档操作日志 - 缺少documentId参数")
    void testListDocumentsLog_MissingParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/logs/documents")
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(logService, never()).getAllLogsByTargetId(anyLong());
    }

    @Test
    @Order(23)
    @DisplayName("测试23：获取文档操作日志 - 服务异常导致500返回")
    void testListDocumentsLog_ServiceException() throws Exception {
        Long documentId = 123L;
        when(logService.getAllLogsByTargetId(documentId))
                .thenThrow(new RuntimeException("DB connection failed"));

        mockMvc.perform(get("/api/v1/logs/documents")
                        .param("documentId", String.valueOf(documentId))
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(logService, times(1)).getAllLogsByTargetId(documentId);
    }

}
