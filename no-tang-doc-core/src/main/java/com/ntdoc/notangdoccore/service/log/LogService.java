package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;

    @Autowired
    @Qualifier("weeklyStrategy")
    private LogGroupStrategy weeklyStrategy;

    @Autowired
    @Qualifier("monthlyStrategy")
    private LogGroupStrategy monthlyStrategy;

    public LogGroupStrategy getLogGroupStrategy(String period){
        switch (period){
            case "week": return weeklyStrategy;
            case "month": return monthlyStrategy;
            default: throw new IllegalArgumentException("Invalid period");
        }
    }

    public List<Log> getAllLogsByUsername(String username){
        return logRepository.findByActorName(username);
    }

    public Map<String,Long> getLogsCountByUser(Long userId,String period){
        LogGroupStrategy logGroupStrategy = getLogGroupStrategy(period);
        return logGroupStrategy.groupLogs(userId, Instant.now());
    }

    public List<Log> getAllLogsByUserId(Long userId){
        return logRepository.findByUserId(userId);
    }

}
