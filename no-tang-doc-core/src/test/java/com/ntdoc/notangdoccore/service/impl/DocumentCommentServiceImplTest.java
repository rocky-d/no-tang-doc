package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.*;
import com.ntdoc.notangdoccore.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DocumentCommentServiceImpl服务测试")
class DocumentCommentServiceImplTest {

    @Mock
    private DocumentCommentRepository commentRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @InjectMocks
    private DocumentCommentServiceImpl commentService;

    private User mockUser;
    private Document mockDocument;
    private Team mockTeam;
    private DocumentComment mockComment;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).kcUserId("kc-123").username("tester").build();
        mockDocument = Document.builder().id(10L).originalFilename("file.pdf").build();
        mockTeam = Team.builder().id(99L).name("TeamA").build();
        mockComment = DocumentComment.builder()
                .id(5L)
                .document(mockDocument)
                .user(mockUser)
                .content("Hello")
                .status(DocumentComment.CommentStatus.ACTIVE)
                .build();
    }

    // ---------------- createComment ----------------
    @Test
    @Order(1)
    @DisplayName("测试1：创建评论 - 成功（无团队）")
    void testCreateComment_Success_NoTeam() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(commentRepository.save(any(DocumentComment.class)))
                .thenAnswer(i -> {
                    DocumentComment c = i.getArgument(0);
                    c.setId(123L);
                    return c;
                });

        DocumentComment result = commentService.createComment(10L, null, "Nice doc", null, "kc-123");

        assertThat(result.getContent()).isEqualTo("Nice doc");
        verify(commentRepository).save(any(DocumentComment.class));
    }

    @Test
    @Order(2)
    @DisplayName("测试2：创建评论 - 失败 - 用户非团队成员")
    void testCreateComment_Fail_NotTeamMember() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(teamRepository.findById(99L)).thenReturn(Optional.of(mockTeam));
        when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockUser, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> commentService.createComment(10L, 99L, "Hello", null, "kc-123"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("您不是该团队成员");
    }

    @Test
    @Order(3)
    @DisplayName("测试3：创建回复评论 - 成功")
    void testCreateComment_Success_WithParent() {
        DocumentComment parent = DocumentComment.builder().id(8L).document(mockDocument).build();

        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(commentRepository.findById(8L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(DocumentComment.class))).thenAnswer(i -> i.getArgument(0));

        DocumentComment result = commentService.createComment(10L, null, "Reply", 8L, "kc-123");
        assertThat(result.getParentComment()).isEqualTo(parent);
    }

    @Test
    @Order(4)
    @DisplayName("测试4：创建回复 - 失败 - 父评论不属于该文档")
    void testCreateComment_Fail_ParentNotMatch() {
        Document anotherDoc = Document.builder().id(77L).build();
        DocumentComment parent = DocumentComment.builder().id(8L).document(anotherDoc).build();

        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(commentRepository.findById(8L)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> commentService.createComment(10L, null, "Bad reply", 8L, "kc-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("父评论不属于该文档");
    }

    @Test
    @Order(5)
    @DisplayName("测试5：创建评论 - 失败 - 父评论不存在")
    void testCreateComment_Fail_ParentCommentNotFound() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(commentRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(10L, null, "reply", 8L, "kc-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("父评论不存在");
    }

    // ---------------- updateComment ----------------

    @Test
    @Order(10)
    @DisplayName("测试10：更新评论 - 成功")
    void testUpdateComment_Success() {
        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(commentRepository.save(any(DocumentComment.class))).thenAnswer(i -> i.getArgument(0));

        DocumentComment result = commentService.updateComment(5L, "Updated", "kc-123");

        assertThat(result.getContent()).isEqualTo("Updated");
    }

    @Test
    @Order(11)
    @DisplayName("测试11：更新评论 - 失败 - 非作者编辑")
    void testUpdateComment_Fail_NotOwner() {
        User other = User.builder().id(9L).kcUserId("kc-999").build();
        mockComment.setUser(other);

        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> commentService.updateComment(5L, "Hack edit", "kc-123"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只能编辑自己的评论");
    }

    @Test
    @Order(12)
    @DisplayName("测试12：更新评论 - 失败 - 评论状态无效")
    void testUpdateComment_Fail_StatusInvalid() {
        mockComment.setStatus(DocumentComment.CommentStatus.DELETED);

        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> commentService.updateComment(5L, "Edit", "kc-123"))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------------- deleteComment ----------------

    @Test
    @Order(20)
    @DisplayName("测试20：删除评论 - 成功 - 作者删除自己的评论")
    void testDeleteComment_Success_Owner() {
        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));

        commentService.deleteComment(5L, "kc-123");
        verify(commentRepository).save(any(DocumentComment.class));
    }

    @Test
    @Order(21)
    @DisplayName("测试21：删除评论 - 成功 - 团队管理员删除")
    void testDeleteComment_Success_TeamAdmin() {
        mockComment.setTeam(mockTeam);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(teamMemberRepository.existsByTeamAndUserAndRoleInAndStatus(any(), any(), anyList(), any()))
                .thenReturn(true);

        commentService.deleteComment(5L, "kc-123");
        verify(commentRepository).save(any(DocumentComment.class));
    }

    @Test
    @Order(22)
    @DisplayName("测试22：删除评论 - 失败 - 非团队Owner或Admin用户无法删除评论")
    void testDeleteComment_Fail_TeamMemberWithoutPermission() {
        // 评论作者是其他人
        User author = User.builder().id(999L).kcUserId("kc-999").username("author").build();
        mockComment.setUser(author);
        mockComment.setTeam(mockTeam);

        // 当前用户是普通成员
        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));

        // Mock：用户是团队成员，但不是OWNER或ADMIN
        when(teamMemberRepository.existsByTeamAndUserAndRoleInAndStatus(
                eq(mockTeam),
                eq(mockUser),
                anyList(),
                eq(TeamMember.MemberStatus.ACTIVE)
        )).thenReturn(false);

        // 验证抛出权限异常
        assertThatThrownBy(() -> commentService.deleteComment(5L, "kc-123"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("没有权限删除该评论");

        // 确认没有保存删除状态
        verify(commentRepository, never()).save(any());
    }

    @Test
    @Order(23)
    @DisplayName("测试23：删除评论 - 失败 - 评论不存在")
    void testDeleteComment_Fail_CommentNotFound() {
        when(commentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commentService.deleteComment(5L, "kc-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("评论不存在");
    }

    // ---------------- getDocumentComments ----------------

    @Test
    @Order(30)
    @DisplayName("测试30：获取文档评论 - 成功 - 无团队")
    void testGetDocumentComments_Success_NoTeam() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(commentRepository.findByDocumentAndStatusOrderByCreatedAtDesc(any(), any()))
                .thenReturn(List.of(mockComment));

        List<DocumentComment> result = commentService.getDocumentComments(10L, null, "kc-123");

        assertThat(result).hasSize(1);
    }

    @Test
    @Order(31)
    @DisplayName("测试31：获取团队评论 - 成功")
    void testGetDocumentComments_Success_WithTeam() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(teamRepository.findById(99L)).thenReturn(Optional.of(mockTeam));
        when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockUser, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(true);
        when(commentRepository.findByDocumentAndTeamAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(List.of(mockComment));

        List<DocumentComment> result = commentService.getDocumentComments(10L, 99L, "kc-123");
        assertThat(result).hasSize(1);
    }

    @Test
    @Order(32)
    @DisplayName("测试32：获取团队评论 - 失败 - 非成员")
    void testGetDocumentComments_Fail_NotMember() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(teamRepository.findById(99L)).thenReturn(Optional.of(mockTeam));
        when(teamMemberRepository.existsByTeamAndUserAndStatus(mockTeam, mockUser, TeamMember.MemberStatus.ACTIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> commentService.getDocumentComments(10L, 99L, "kc-123"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("无法查看团队评论");
    }

    @Test
    @Order(33)
    @DisplayName("测试33：获取团队评论 - 失败 - 团队不存在")
    void testGetDocumentComments_Fail_TeamNotFound() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(mockUser));
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getDocumentComments(10L, 99L, "kc-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("团队不存在");
    }

    // ---------------- getCommentById ----------------

    @Test
    @Order(40)
    @DisplayName("测试40：根据ID获取评论 - 成功")
    void testGetCommentById_Success() {
        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        DocumentComment result = commentService.getCommentById(5L);
        assertThat(result).isEqualTo(mockComment);
    }

    @Test
    @Order(41)
    @DisplayName("测试41：根据ID获取评论 - 失败 - 不存在")
    void testGetCommentById_Fail_NotFound() {
        when(commentRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commentService.getCommentById(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("评论不存在");
    }

    // ---------------- getCommentReplies ----------------

    @Test
    @Order(50)
    @DisplayName("测试50：获取评论回复 - 成功")
    void testGetCommentReplies_Success() {
        when(commentRepository.findById(5L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.findByParentCommentAndStatusOrderByCreatedAtAsc(mockComment, DocumentComment.CommentStatus.ACTIVE))
                .thenReturn(List.of(mockComment));

        List<DocumentComment> replies = commentService.getCommentReplies(5L);
        assertThat(replies).hasSize(1);
    }
}
