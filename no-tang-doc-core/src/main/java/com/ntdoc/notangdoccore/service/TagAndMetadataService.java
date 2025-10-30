package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.Tag;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagAndMetadataService {
    private final DocumentRepository documentRepo;
    private final TagRepository tagRepository;

    @Transactional
    public Document upsertTagsAndMetadata(Long documentId, List<String> tags, Map<String, String> metadata) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));
        Set<Tag> tagList = convertStringsToTags(tags);
        document.getTags().addAll(tagList);

        if (metadata != null) {
            metadata.forEach(document::putMetadata);
        }
        return documentRepo.saveAndFlush(document);//May need to adjust
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
