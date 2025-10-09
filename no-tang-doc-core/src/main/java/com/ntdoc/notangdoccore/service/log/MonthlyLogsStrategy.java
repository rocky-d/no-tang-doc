package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.repository.LogRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component("monthlyStrategy")
public class MonthlyLogsStrategy implements LogGroupStrategy{
    private LogRepository logRepository;

    @Override
    public Map<String,Long> groupLogs(Long userId, Instant endTime){
        //返回过去30天
        Instant startTime  = endTime.minus(Duration.ofDays(30));

        return logRepository.countByWeek(userId,startTime,endTime);
    }
}
