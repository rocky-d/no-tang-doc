package com.ntdoc.notangdoccore.service.log;

import com.ntdoc.notangdoccore.entity.Log;

/*TODO:
*  1.Log 策略用于设计不同的日志存储方式
*  2.策略方法仍需定义
* */

public interface LogStrategy {
    void saveLog(Log log) throws Exception;
}
