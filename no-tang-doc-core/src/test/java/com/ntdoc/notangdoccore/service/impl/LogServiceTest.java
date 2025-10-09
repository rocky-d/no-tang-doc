package com.ntdoc.notangdoccore.service.impl;


import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationStatus;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import com.ntdoc.notangdoccore.repository.LogRepository;
import com.ntdoc.notangdoccore.service.log.LogGroupStrategy;
import com.ntdoc.notangdoccore.service.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {
    @Mock
    private LogRepository logRepository;

    @Mock
    private LogGroupStrategy weeklyStrategy;

    @Mock
    private LogGroupStrategy monthlyStrategy;

    @InjectMocks
    private LogService logService;

    private List<Log> mockLogs;
    private Map<String,Long> mockGroupLogs;

    private Log createMockLog(Long id, ActorType actorType, String actorName, Long userId, OperationType operationType,
                              String targetName, OperationStatus operationStatus) {
        Log log = new Log();
        log.setId(id);
        log.setActorType(actorType);
        log.setActorName(actorName);
        log.setUserId(userId);
        log.setOperationType(operationType);
        log.setTargetName(targetName);
        log.setOperationStatus(operationStatus);
        log.setMessage("");
        log.setTime(Instant.now());

        return log;
    }

    @BeforeEach
    void setUp(){
        logService = new LogService(logRepository);

        // Set Strategy
        try{
            var weeklyField = LogService.class.getDeclaredField("weeklyStrategy");
            weeklyField.setAccessible(true);
            weeklyField.set(logService,weeklyStrategy);

            var monthlyField = LogService.class.getDeclaredField("monthlyStrategy");
            monthlyField.setAccessible(true);
            monthlyField.set(logService,monthlyStrategy);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        mockLogs = Arrays.asList(
                createMockLog(1L,ActorType.USER,"test1",1L,OperationType.UPLOAD_DOCUMENT,"testFile",
                        OperationStatus.SUCCESS),
                createMockLog(2L,ActorType.USER,"test1",1L,OperationType.UPDATE_DOCUMENT,"testFile",
                        OperationStatus.SUCCESS),
                createMockLog(3L,ActorType.USER,"test1",1L,OperationType.DELETE_DOCUMENT,"testFile",
                        OperationStatus.SUCCESS),
                createMockLog(4L,ActorType.USER,"test2",2L,OperationType.UPLOAD_DOCUMENT,"testFile2",
                        OperationStatus.SUCCESS),
                createMockLog(5L,ActorType.USER,"test2",2L,OperationType.UPDATE_DOCUMENT,"testFile2",
                        OperationStatus.FAILED)
        );
        mockGroupLogs = new HashMap<>();
        mockGroupLogs.put("2025-10-7",2L);
        mockGroupLogs.put("Week19",3L);
    }

    //测试用户能正确设置周策略
    @Test
    void testGetLogGroupStrategy_WithWeek_ReturnWeeklyStrategy(){
        LogGroupStrategy result = logService.getLogGroupStrategy("week");

        assertNotNull(result);
        assertEquals(weeklyStrategy,result);
    }

    //测试用户能正确设置月策略
    @Test
    void testGetLogGroupStrategy_WithMonth_ReturnMonthlyStrategy(){
        LogGroupStrategy result = logService.getLogGroupStrategy("month");

        assertNotNull(result);
        assertEquals(monthlyStrategy,result);
    }

    //测试用户不能设置非法策略
    @Test
    void testGetLogGroupStrategy_WithInvalidPeriod_ShouldThrowException(){
        IllegalArgumentException exception =  assertThrows(
                IllegalArgumentException.class,()->logService.getLogGroupStrategy("invalid")
        );

        assertEquals("Invalid period", exception.getMessage());
    }

    //测试用户正确通过UserId获取所有日志
    @Test
    void testGetAllLogsByUserId_ShouldReturnAllLogs(){
        // 设置Repostitory的返回
        Long userId = 1L;
        List<Log> expactedLogs = mockLogs.subList(0,3);
        when(logRepository.findByUserId(userId)).thenReturn(expactedLogs);

        List<Log> result = logService.getAllLogsByUserId(userId);

        assertNotNull(result);
        assertEquals(3,result.size());
        assertEquals(expactedLogs,result);
        verify(logRepository,times(1)).findByUserId(userId);
    }

    //测试用户无日志时候的返回
    @Test
    void testGetAllLogsByUserId_WithNoLogs_ShouldReturnEmptyList(){
        Long userId = 100L;
        when(logRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        List<Log> result = logService.getAllLogsByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(logRepository,times(1)).findByUserId(userId);
    }

    //测试用户按周返回日志条数
    @Test
    void testGetLogsCountByUser_WithWeekPeriod_ShouldReturnWeeklyGroupedLogs(){
        Long userId = 1L;
        String period = "week";
        when(weeklyStrategy.groupLogs(anyLong(),any(Instant.class))).thenReturn(mockGroupLogs);

        Map<String,Long> result = logService.getLogsCountByUser(userId,period);

        assertNotNull(result);
        assertEquals(mockGroupLogs,result);
        verify(weeklyStrategy,times(1)).groupLogs(eq(userId),any(Instant.class));
        verify(monthlyStrategy,never()).groupLogs(anyLong(),any(Instant.class));
    }

    //测试用户按月返回日志条数
    @Test
    void testGetLogsCountByUser_WithMonthPeriod_ShouldReturnMonthlyGroupedLogs(){
        Long userId = 1L;
        String period = "month";
        when(monthlyStrategy.groupLogs(anyLong(),any(Instant.class))).thenReturn(mockGroupLogs);

        Map<String,Long> result = logService.getLogsCountByUser(userId,period);

        assertNotNull(result);
        assertEquals(mockGroupLogs,result);
        verify(monthlyStrategy,times(1)).groupLogs(eq(userId),any(Instant.class));
        verify(weeklyStrategy,never()).groupLogs(anyLong(),any(Instant.class));
    }

    //测试用户不能以无效字段返回日志条数
    @Test
    void testGetLogsCountByUser_WithInvalidPeriod_ShouldThrowException(){
        Long userId = 1L;
        String period = "invalid";

        assertThrows(IllegalArgumentException.class,()->logService.getLogsCountByUser(userId,period));
    }

}
