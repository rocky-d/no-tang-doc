package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.dto.log.LogsCountDTO;
import com.ntdoc.notangdoccore.entity.Log;

import java.time.Instant;
import java.util.*;

public interface LogGroupStrategy {

    Map<String, Long> groupLogs(Long userId, Instant endTime);

    default Map<String, Long> format(List<LogsCountDTO> logs){
        if(logs == null || logs.isEmpty()){
            return Collections.emptyMap();
        }
        Map<String, Long> result = new LinkedHashMap<>();
        for(LogsCountDTO log : logs){
            if (log == null) continue;
            String label = log.label();
            Long count = log.count();
            if (count == null || label == null) continue;

            result.merge(label, count, Long::sum);
        }
        return result;
    };
}
