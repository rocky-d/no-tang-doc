package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagAndMetadataService {
    private final DocumentRepository documentRepo;

    @Transactional
    public Document upsertTagsAndMetadata(Long documentId, List<String> tags, Map<String, String> metadata) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));
        if (tags != null) {
            tags.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(document::addTag);
        }

        if (metadata != null) {
            metadata.forEach(document::putMetadata);
        }
        return documentRepo.saveAndFlush(document);//May need to adjust
    }

}
