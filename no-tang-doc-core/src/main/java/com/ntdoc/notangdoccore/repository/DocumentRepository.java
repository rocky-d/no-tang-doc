package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 文档数据访问层
 */
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    /**
     * 根据用户和状态查找文档
     */
    List<Document> findByUploadedByAndStatusOrderByCreatedAtDesc(User uploadedBy, Document.DocumentStatus status);

    /**
     * 根据用户查找所有文档
     */
    List<Document> findByUploadedByOrderByCreatedAtDesc(User uploadedBy);

    /**
     * 根据原始文件名查找文件（模糊匹配、不区分大小写）
     */
    List<Document> findByUploadedByAndOriginalFilenameContainingIgnoreCaseOrderByCreatedAtDesc(User uploadedBy, String partialFilename);

    /**
     * 根据S3键查找文档
     */
    Optional<Document> findByS3Key(String s3Key);

    /**
     * 根据存储文件名查找文档
     */
    Optional<Document> findByStoredFilename(String storedFilename);

    /**
     * 根据文件哈希查找重复文件
     */
    Optional<Document> findByFileHashAndUploadedBy(String fileHash, User uploadedBy);

    /**
     * 统计用户的活跃文档数量
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.uploadedBy = :user AND d.status = :status")
    long countByUploadedByAndStatus(@Param("user") User user, @Param("status") Document.DocumentStatus status);

    /**
     * 查找用户最近上传的文档
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedBy = :user AND d.status = 'ACTIVE' ORDER BY d.createdAt DESC")
    List<Document> findRecentDocumentsByUser(@Param("user") User user);
}
