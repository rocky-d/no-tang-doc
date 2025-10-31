package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.Tag;
import com.ntdoc.notangdoccore.repository.DocumentRepository;
import com.ntdoc.notangdoccore.repository.TagRepository;
import com.ntdoc.notangdoccore.service.DocumentTagService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentTagServiceImplTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private DocumentTagServiceImpl documentTagService;

    private Document mockDocument;

    private Tag mockTag1,mockTag2;

    @BeforeEach
    void setUp() {
        mockTag1 = Tag.builder().id(1L).tag("AI").documents(new HashSet<>()).build();
        mockTag2 = Tag.builder().id(2L).tag("ML").documents(new HashSet<>()).build();

        mockDocument = Document.builder()
                .id(100L)
                .originalFilename("test.pdf")
                .tags(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("addTags - 成功添加标签")
    void addTags_Success() {
        when(documentRepository.findById(100L)).thenReturn(Optional.of(mockDocument));
        when(tagRepository.findByTag("AI")).thenReturn(Optional.of(mockTag1));
        when(tagRepository.findByTag("ML")).thenReturn(Optional.of(mockTag2));
        when(documentRepository.save(Mockito.<Document>any()))
                .thenAnswer(invocation -> invocation.getArgument(0, Document.class));


        Document result = documentTagService.addTags(100L, List.of("AI", "ML"), "user-123");

        assertThat(result.getTags()).hasSize(2);
        verify(documentRepository).save(Mockito.<Document>any());
        verify(tagRepository, times(2)).findByTag(anyString());
    }

    @Test
    @DisplayName("addTags - 文档不存在抛异常")
    void addTags_DocumentNotFound() {
        when(documentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> documentTagService.addTags(100L, List.of("AI"), "user-123"));

        verify(documentRepository).findById(100L);
        verifyNoInteractions(tagRepository);
    }

    @Test
    @DisplayName("removeTag - 成功移除标签")
    void removeTag_Success() {
        mockDocument.getTags().add(mockTag1);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(mockDocument));
        when(tagRepository.findByTag("AI")).thenReturn(Optional.of(mockTag1));
        when(documentRepository.save(Mockito.<Document>any())).thenAnswer(inv -> inv.getArgument(0));

        Document result = documentTagService.removeTag(100L, "AI", "user-123");

        assertThat(result.getTags()).isEmpty();
        verify(documentRepository).save(Mockito.<Document>any());

    }

    @Test
    @DisplayName("removeTag - 文档不存在抛异常")
    void removeTag_DocumentNotFound() {
        when(documentRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> documentTagService.removeTag(100L, "AI", "user-123"));
    }

    @Test
    @DisplayName("replaceTags - 成功替换标签")
    void replaceTags_Success() {
        mockDocument.getTags().add(mockTag1);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(mockDocument));
        when(tagRepository.findByTag("ML")).thenReturn(Optional.of(mockTag2));
        when(documentRepository.save(Mockito.<Document>any())).thenAnswer(inv -> inv.getArgument(0));

        Document result = documentTagService.replaceTags(100L, List.of("ML"), "user-123");

        assertThat(result.getTags()).containsExactly(mockTag2);
        verify(documentRepository,times(2)).save(Mockito.<Document>any());
    }

    @Test
    @DisplayName("getTags - 成功获取标签列表")
    void getTags_Success() {
        mockDocument.getTags().add(mockTag1);
        mockDocument.getTags().add(mockTag2);

        when(documentRepository.findById(100L)).thenReturn(Optional.of(mockDocument));

        List<Tag> tags = documentTagService.getTags(100L);

        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(Tag::getTag).containsExactlyInAnyOrder("AI", "ML");
    }

    @Test
    @DisplayName("getTags - 文档不存在返回空列表")
    void getTags_DocumentNotFound_ReturnEmpty() {
        when(documentRepository.findById(100L)).thenReturn(Optional.empty());

        List<Tag> tags = documentTagService.getTags(100L);

        assertThat(tags).isEmpty();
    }

    @Test
    @DisplayName("getDocumentsByTag - 成功获取文档列表")
    void getDocumentsByTag_Success() {
        mockTag1.setDocuments(Set.of(mockDocument));
        when(tagRepository.findByTag("AI")).thenReturn(Optional.of(mockTag1));

        List<Document> result = documentTagService.getDocumentsByTag("AI");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        verify(tagRepository).findByTag("AI");
    }

    @Test
    @DisplayName("getDocumentsByTag - 标签不存在返回空列表")
    void getDocumentsByTag_TagNotFound() {
        when(tagRepository.findByTag("Unknown")).thenReturn(Optional.empty());
        List<Document> result = documentTagService.getDocumentsByTag("Unknown");
        assertThat(result).isEmpty();
    }

    // ==================== convertStringsToTags ====================
    @Test
    @DisplayName("convertStringsToTags - 成功创建新标签")
    void convertStringsToTags_Success() {
        when(tagRepository.findByTag("AI")).thenReturn(Optional.empty());
        when(tagRepository.save(Mockito.<Tag>any()))
                .thenAnswer(inv -> {
                    Tag t = inv.getArgument(0);
                    t.setId(1L);
                    return t;
                });

        Set<Tag> tags = documentTagService.convertStringsToTags(List.of("AI"));
        assertThat(tags).hasSize(1);
        assertThat(tags.iterator().next().getTag()).isEqualTo("AI");


        verify(tagRepository).save(Mockito.<Tag>any());

    }

    @Test

    @DisplayName("convertStringsToTags - 空输入返回空集合")
    void convertStringsToTags_EmptyInput() {
        Set<Tag> tags = documentTagService.convertStringsToTags(Collections.emptyList());
        assertThat(tags).isEmpty();
    }

}
