package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class DocumentSpecification {
    public static Specification<Document> uploadedBy(User user) {
        return (root, query, cb) -> cb.equal(root.get("uploadedBy"), user);
    }

    public static Specification<Document> fileTypeEquals(String contentType) {
        return (root, query, cb) -> cb.equal(root.get("contentType"), contentType);
    }

    public static Specification<Document> uploadedAfter(Instant start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start);
    }

    public static Specification<Document> uploadedBefore(Instant end) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), end);
    }

}
