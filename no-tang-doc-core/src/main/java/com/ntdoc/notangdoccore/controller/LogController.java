package com.ntdoc.notangdoccore.controller;

import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.service.LogService;
import com.ntdoc.notangdoccore.service.UserSyncService;
import com.ntdoc.notangdoccore.service.impl.UserSyncServiceImpl;
import com.ntdoc.notangdoccore.service.impl.LogServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Validated
@Tag(name="日志管理",description = "日志查看,条目获取")
public class LogController {
    private final UserSyncService userSyncService;
    private final LogService logService;

    /*
    * 获取当前用户的所有日志
    * */
    @GetMapping("/list")
    public ResponseEntity<List<Log>> listLogs(@AuthenticationPrincipal Jwt jwt) {
        try{
            log.info("Receive list all logs request");
            User user = userSyncService.ensureFromJwt(jwt);

            List<Log> logList = logService.getAllLogsByUserId(user.getId());

            log.info("Get all logs successfully");

            return ResponseEntity.ok(logList);
        }catch(Exception e){
            log.info("Get all logs failed",e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

        }
    }


    /*
    * 获取当前用户的日志条数
    * 可以设置每周或者每月
    */
    @GetMapping("/count")
    public ResponseEntity<Map<String,Long>> getLogsCount(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestParam(defaultValue = "week")
                                                         @Pattern(regexp = "^(week|month)$",message = "period can only be week or month")
                                                         String period) {
        try{
            log.info("Receive list {} logs request", period);
            User user = userSyncService.ensureFromJwt(jwt);

            Map<String,Long> logsCount = logService.getLogsCountByUser(user.getId(),period);

            log.info("Get {} logs successfully",period);

            return ResponseEntity.ok(logsCount);
        }catch (Exception e){
            log.info("Get {} logs failed",period,e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /*
     * 获取当前文档的操作日志，查看对文档操作的log记录
     */
    @GetMapping("/documents")
    public ResponseEntity<List<Log>> listDocumentsLog(@AuthenticationPrincipal Jwt jwt,@RequestParam Long documentId){
        try{
            log.info("Receive list documents log request");

            List<Log> documentLogList = logService.getAllLogsByTargetId(documentId);
            log.info("Get all documents log successfully");
            return ResponseEntity.ok(documentLogList);
        }catch (Exception e){
            log.info("Get all documents log failed：{}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
