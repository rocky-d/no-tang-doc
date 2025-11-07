package com.ntdoc.notangdoccore.entity;


import com.ntdoc.notangdoccore.entity.logenum.ActorType;
import com.ntdoc.notangdoccore.entity.logenum.OperationStatus;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private ActorType actorType;

    @Column
    private String actorName;

    @Column
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column
    private OperationType operationType;

    @Column
    private Long targetId;

    @Column
    private String targetName;

    @Enumerated(EnumType.STRING)
    @Column
    private OperationStatus operationStatus;

    @Column(name="message",columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column
    private Instant time;
}
