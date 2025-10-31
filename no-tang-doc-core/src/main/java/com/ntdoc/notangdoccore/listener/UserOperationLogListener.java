package com.ntdoc.notangdoccore.listener;

import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.event.UserOperationEvent;
import com.ntdoc.notangdoccore.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserOperationLogListener {
    private final LogRepository logRepository;

    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void handleUserOperation(UserOperationEvent event){
        // 记录当前线程
        log.info("Async thread: {}",Thread.currentThread().getName());

        try{
            Log logEntity = new Log();
            logEntity.setUserId(event.getUserId());
            logEntity.setTargetId(event.getTargetId());
            logEntity.setActorType(event.getActorType());
            logEntity.setActorName(event.getActorName());
            logEntity.setOperationType(event.getOperationType());
            logEntity.setTargetName(event.getTargetName());
            logEntity.setOperationStatus(event.getOperationStatus());
            logEntity.setMessage(event.getMessage());
            logEntity.setTime(Instant.ofEpochMilli(event.getTimestamp()));

            logRepository.save(logEntity);

            log.info("User Log have been recorded: {} - {} - {} - {}",
                    event.getActorName(),
                    event.getOperationType(),
                    event.getTargetName(),
                    event.getOperationStatus());
        }catch(Exception e){
            log.error("Failed to save user operation log", e);
        }
    }
}
