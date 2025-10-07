package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Log;
import com.ntdoc.notangdoccore.entity.logenum.OperationStatus;
import com.ntdoc.notangdoccore.entity.logenum.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface LogRepository extends JpaRepository<Log,Long> {

    List<Log> findByActorName(String actorName);
    List<Log> findByUserId(Long userId);
    List<Log> findByOperationType(OperationType operationType);
    List<Log> findByTimestampBetween(Instant start, Instant end);
    List<Log> findByOperationStatus(OperationStatus status);

    @Query("SELECT DATE_FORMAT(l.timestamp,'%Y-%m-%d') as label,COUNT(l) as count FROM Log l WHERE l.userId = :userId AND l.timestamp BETWEEN :start AND :end GROUP BY DATE_FORMAT(l.timestamp,'%Y-%m-%d')")
    Map<String,Long> countByDay(@Param("userId") Long uesrId,@Param("start") Instant start,@Param("end") Instant end);

    @Query("SELECT CONCAT('Week',WEEK(l.timestamp)) as label,COUNT(l) as count FROM Log l WHERE l.userId = :userId AND l.timestamp BETWEEN :start AND :end GROUP BY WEEK(l.timestamp)")
    Map<String,Long> countByWeek(@Param("userId") Long userId,@Param("start") Instant start,@Param("end") Instant end);

}
