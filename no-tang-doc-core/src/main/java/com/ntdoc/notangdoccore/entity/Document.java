package com.ntdoc.notangdoccore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

/**
 * Document entity class
 * Store the metadata information of the document
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document", indexes = {
        @Index(name = "idx_document_user_id", columnList = "user_id"),
        @Index(name = "idx_document_created_at", columnList = "created_at"),
        @Index(name = "idx_document_status", columnList = "status")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 255, unique = true)
    private String storedFilename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "s3_bucket", nullable = false, length = 100)
    private String s3Bucket;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(length = 500)
    private String description;

    //Tags
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "document_id", foreignKey = @ForeignKey(name = "fk_document_tags_document")),
            inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "fk_document_tag_tag")),
            uniqueConstraints = @UniqueConstraint(name = "uk_document_tag", columnNames = {"document_id", "tag_id"})
    )
    @Builder.Default
    private Set<Tag> tags = new LinkedHashSet<>();

    public void addTag(Tag tag) {
        if (tag != null) {
            tags.add(tag);
            tag.getDocuments().add(this); // 双向维护
        }
    }

    public void removeTag(Tag tag) {
        if (tag != null) {
            tags.remove(tag);
            tag.getDocuments().remove(this);
        }
    }

    //Metadata
    @ElementCollection
    @CollectionTable(
            name = "document_metadata",
            joinColumns = @JoinColumn(name = "document_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "meta_key"})
    )
    @MapKeyColumn(name = "meta_key", length = 128)
    @Column(name = "meta_value", length = 1024, nullable = false)
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();

    public void putMetadata(String key, String value) {
        if (key != null && !key.isBlank()) {
            metadata.put(key.trim(), value == null ? "" : value.trim());
        }
    }

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    public enum DocumentStatus {
        UPLOADING,
        ACTIVE,
        DELETED,
        PROCESSING
    }
}
