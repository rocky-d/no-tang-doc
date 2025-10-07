package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;

/**
 * 文档业务服务接口
 */
public interface DocumentService {

    /**
     * 上传文档
     *
     * @param file 要上传的文件
     * @param kcUserId Keycloak 用户ID
     * @param description 文档描述
     * @return 保存的文档实体
     */
    Document uploadDocument(MultipartFile file, String kcUserId, String description);

    /**
     * 获取文档下载URL
     *
     * @param documentId 文档ID
     * @param kcUserId 当前用户ID（用于权限验证）
     * @return 下载URL
     */
    URL getDownloadUrl(Long documentId, String kcUserId);

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
     * 删除文档
     *
     * @param documentId 文档ID
     * @param kcUserId 当前用户ID（用于权限验证）
     */
    void deleteDocument(Long documentId, String kcUserId);

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
}
