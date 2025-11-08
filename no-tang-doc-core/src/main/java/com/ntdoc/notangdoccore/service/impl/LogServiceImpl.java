package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.repository.LogRepository;
import com.ntdoc.notangdoccore.service.LogService;
import com.ntdoc.notangdoccore.service.log.LogGroupStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
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

    @Override
    public List<Log> getAllLogsByUsername(String username){
        return logRepository.findByActorName(username);
    }

    @Override
    public List<Log> getAllLogsByTargetId(Long targetId){
        return logRepository.findByTargetId(targetId);
    }

    @Override
    public Map<String,Long> getLogsCountByUser(Long userId,String period){
        LogGroupStrategy logGroupStrategy = getLogGroupStrategy(period);
        return logGroupStrategy.groupLogs(userId, Instant.now());
    }

    @Override
    public List<Log> getAllLogsByUserId(Long userId){
        return logRepository.findByUserId(userId);
    }
}
