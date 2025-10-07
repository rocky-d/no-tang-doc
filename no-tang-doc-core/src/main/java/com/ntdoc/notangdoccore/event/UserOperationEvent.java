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
    private final OperationType operationType;
    private final String targetName;
    private final OperationStatus operationStatus;
    private final String message;
    private final Instant timestamp;

    public UserOperationEvent(Object source,
                              ActorType actorType,
                              String actorName,
                              OperationType operationType,
                              String targetName,
                              OperationStatus operationStatus,
                              String message){
        super(source);
        this.actorType = actorType;
        this.actorName = actorName;
        this.operationType = operationType;
        this.targetName = targetName;
        this.operationStatus = operationStatus;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // success
    public static UserOperationEvent success(Object source,
                                             ActorType actorType,
                                             String username,
                                             OperationType operationType,
                                             String targetName){
        return new UserOperationEvent(source, actorType, username, operationType, targetName, OperationStatus.SUCCESS, null);
    }


    //fail
    public static UserOperationEvent fail(Object source,
                                             ActorType actorType,
                                             String username,
                                             OperationType operationType,
                                             String targetName,
                                             String message){
        return new UserOperationEvent(source, actorType, username, operationType, targetName, OperationStatus.FAILED, null);
    }
}
