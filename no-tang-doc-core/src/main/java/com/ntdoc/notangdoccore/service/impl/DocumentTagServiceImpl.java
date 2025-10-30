package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.Tag;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.TagRepository;
import com.ntdoc.notangdoccore.service.DocumentService;
import com.ntdoc.notangdoccore.service.DocumentTagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DocumentTagServiceImpl implements DocumentTagService {
    private final DocumentRepository documentRepository;
    private final TagRepository tagRepository;

    @Override
    public Document addTags(Long documentId, List<String> tagNames, String kcUserId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        Set<Tag> tags = convertStringsToTags(tagNames);
        document.getTags().addAll(tags);

        return documentRepository.save(document);
    }

    @Override
    public Document removeTag(Long documentId, String tagName, String kcUserId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        tagRepository.findByTag(tagName).ifPresent(document::removeTag);
        return documentRepository.save(document);
    }

    @Override
    public Document replaceTags(Long documentId, List<String> tagNames, String kcUserId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        document.getTags().clear();
        addTags(documentId, tagNames, kcUserId);
        return documentRepository.save(document);
    }

    @Override
    public List<Tag> getTags(Long documentId) {
        return documentRepository.findById(documentId)
                .map(d -> new ArrayList<>(d.getTags()))
                .orElseGet(ArrayList::new);
    }

    @Override
    public List<Document> getDocumentsByTag(String tagName) {
        return tagRepository.findByTag(tagName)
                .map(Tag::getDocuments)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }

    public Set<Tag> convertStringsToTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) continue;
            String normalized = tagName.trim();

            // 查询数据库中是否已有
            Tag existingTag = tagRepository.findByTag(normalized)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder().tag(normalized).build()
                    ));

            tags.add(existingTag);
        }

        return tags;
    }
}

