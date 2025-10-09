package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.repository.LogRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;


@Component("weeklyStrategy")
public class WeeklyLogsStrategy implements LogGroupStrategy{

    private LogRepository logRepository;

    @Override
    public Map<String,Long> groupLogs(Long userId, Instant endTime){
        //返回过去7天
        Instant startTime  = endTime.minus(Duration.ofDays(7));

        return logRepository.countByDay(userId,startTime,endTime);
    }
}
