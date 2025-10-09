package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.entity.Log;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface LogGroupStrategy {

    Map<String, Long> groupLogs(Long userId, Instant endTime);
}
