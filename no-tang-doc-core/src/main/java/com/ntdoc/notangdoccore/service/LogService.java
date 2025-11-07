package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.Log;

import java.util.List;
import java.util.Map;

public interface LogService {

    List<Log> getAllLogsByUsername(String username);
    List<Log> getAllLogsByTargetId(Long targetId);
    Map<String,Long> getLogsCountByUser(Long userId,String period);
    List<Log> getAllLogsByUserId(Long userId);
}
