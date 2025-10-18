package com.ntdoc.notangdoccore.service;

import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;

/**
 * 文件存储服务接口
 * 提供文档上传、下载、删除等核心功能
 */
public interface FileStorageService {

    /**
     * 上传文件到 DigitalOcean Spaces
     *
     * @param file 要上传的文件
     * @param kcUserId Keycloak 用户ID
     * @return S3 存储键
     */
    String uploadFile(MultipartFile file, String kcUserId);

    /**
     * 生成文件下载的预签名URL
     *
     * @param s3Key 文件的S3键
     * @param expiration 过期时间
     * @return 预签名下载URL
     */
    URL generateDownloadUrl(String s3Key, Duration expiration);

    /**
     * 生成分享的预签名URL，用户只能网页浏览，不能下载
     *
     * @param s3Key 文件的S3键
     * @param expiration 过期时间
     * @return 预签名浏览URL
     */
    URL generateShareUrl(String s3Key, Duration expiration);


    /**
     * 生成文件上传的预签名URL（用于前端直接上传）
     *
     * @param s3Key 文件的S3键
     * @param contentType 文件类型
     * @param expiration 过期时间
     * @return 预签名上传URL
     */
    URL generateUploadUrl(String s3Key, String contentType, Duration expiration);

    /**
     * 删除文件
     *
     * @param s3Key 文件的S3键
     * @return 是否删除成功
     */
    boolean deleteFile(String s3Key);

    /**
     * 检查文件是否存在
     *
     * @param s3Key 文件的S3键
     * @return 文件是否存在
     */
    boolean fileExists(String s3Key);

    /**
     * 生成存储路径
     *
     * @param kcUserId Keycloak 用户ID
     * @param originalFilename 原始文件名
     * @return 生成的存储路径
     */
    String generateStoragePath(String kcUserId, String originalFilename);
}
