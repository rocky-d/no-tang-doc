package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.dto.document.DocumentDownloadResponse;
import com.ntdoc.notangdoccore.dto.document.DocumentUploadResponse;
import com.ntdoc.notangdoccore.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

/**
 * 文档业务服务接口
 */
public interface DocumentService {

    /**
     * 上传文档
     *
     * @param file 要上传的文件
     * @param fileName 文件名
     * @param description 文档描述
     * @param kcUserId Keycloak 用户ID
     * @return 文档上传响应
     */
    DocumentUploadResponse uploadDocument(MultipartFile file, String fileName, String description, String kcUserId);

    /**
     * 获取文档下载链接
     *
     * @param documentId 文档ID
     * @param kcUserId 当前用户ID（用于权限验证）
     * @return 文档下载响应
     */
    DocumentDownloadResponse getDocumentDownloadUrl(Long documentId, String kcUserId);

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @param kcUserId 当前用户ID（用于权限验证）
     */
    void deleteDocument(Long documentId, String kcUserId);

    /**
     * 获取用户的所有文档
     *
     * @param kcUserId Keycloak 用户ID
     * @return 文档列表
     */
    List<Document> getUserDocuments(String kcUserId);

    /**
     * 获取用户的所有文档，按状态过滤
     *
     * @param kcUserId Keycloak 用户ID
     * @param status 文档状态（可选）
     * @return 文档列表
     */
    List<Document> getUserDocuments(String kcUserId, Document.DocumentStatus status);

    /**
     * 根据ID获取文档详情
     *
     * @param documentId 文档ID
     * @param kcUserId 当前用户ID（用于权限验证）
     * @return 文档实体
     */
    Document getDocumentById(Long documentId, String kcUserId);

    /**
     * 增加下载次数
     *
     * @param documentId 文档ID
     */
    void incrementDownloadCount(Long documentId);

    /**
     * 按文件名搜索
     *
     * @param kcUserId 当前用户ID
     * @param nameOrKeyword 文件名或关键字（不区分大小写）
     * @return 文档列表
     */
    List<Document> searchDocumentsByFilename(String kcUserId, String nameOrKeyword);

    /**
     * 根据所有者、上传日期、文件类型过滤文档
     *
     * @param kcUserId 当前用户ID
     * @param contentType 文件类型，可为空
     * @param start 上传日期的左边界（即查询 createdAt >= start），可为空
     * @param end 上传日期的右边界（即查询createdAt <= end），可为空
     * @return 符合条件的文档列表
     */
    List<Document> filterDocuments(String kcUserId, String contentType, Instant start, Instant end);

}
