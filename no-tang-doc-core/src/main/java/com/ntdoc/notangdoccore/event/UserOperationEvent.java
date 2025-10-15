package com.ntdoc.notangdoccore.event;

import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationStatus;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class UserOperationEvent extends ApplicationEvent {
    private final ActorType actorType;
    private final String actorName;
    private final Long userId;
    private final OperationType operationType;
    private final Long targetId;
    private final String targetName;
    private final OperationStatus operationStatus;
    private final String message;
    private final Instant time;

    public UserOperationEvent(Object source,
                              ActorType actorType,
                              String actorName,
                              Long userId,
                              OperationType operationType,
                              Long targetId,
                              String targetName,
                              OperationStatus operationStatus,
                              String message){
        super(source);
        this.actorType = actorType;
        this.actorName = actorName;
        this.userId = userId;
        this.operationType = operationType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.operationStatus = operationStatus;
        this.message = message;
        this.time = Instant.now();
    }

    // success
    public static UserOperationEvent success(Object source,
                                             ActorType actorType,
                                             String username,
                                             Long userId,
                                             Long targetId,
                                             OperationType operationType,
                                             String targetName){
        return new UserOperationEvent(source, actorType, username, userId,operationType,targetId,targetName,OperationStatus.SUCCESS, null);
    }


    //fail
    public static UserOperationEvent fail(Object source,
                                             ActorType actorType,
                                             String username,
                                             Long userId,
                                             OperationType operationType,
                                             String targetName,
                                             String message){
        return new UserOperationEvent(source, actorType, username, userId,operationType,targetName, OperationStatus.FAILED, null);
    }
}
