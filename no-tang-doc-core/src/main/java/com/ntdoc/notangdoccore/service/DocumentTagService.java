package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.Tag;

import java.util.List;

public interface DocumentTagService {
    /**
     * addTagsToDocument (if not exist auto create)
     */
    Document addTags(Long documentId, List<String> tagNames, String kcUserId);

    /**
     * Delete tag for documents
     */
    Document removeTag(Long documentId, String tagName, String kcUserId);

    /**
     * replace tag
     */
    Document replaceTags(Long documentId, List<String> tagNames, String kcUserId);

    /**
     * get all Tags from document
     */
    List<Tag> getTags(Long documentId);

    /**
     * get all document which have the tag
     */
    List<Document> getDocumentsByTag(String tagName);

}
