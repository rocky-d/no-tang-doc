package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.dto.log.LogsCountDTO;
import com.ntdoc.notangdoccore.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;


@Component("weeklyStrategy")
public class WeeklyLogsStrategy implements LogGroupStrategy{

    @Autowired
    private LogRepository logRepository;

    @Override
    public Map<String,Long> groupLogs(Long userId, Instant endTime){
        //返回过去7天
        Instant startTime  = endTime.minus(Duration.ofDays(7));
        List<LogsCountDTO> logs = logRepository.countByDay(userId,startTime,endTime).stream()
                .map(r -> new LogsCountDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();
        return format(logs);
    }
}
