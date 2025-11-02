package com.ntdoc.notangdoccore.service.impl;

import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.UserRepository;
import com.ntdoc.notangdoccore.service.impl.UserSyncServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UserSyncServiceImpl服务测试")
public class UserSyncServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSyncServiceImpl userSyncService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .kcUserId("kc-123")
                .username("old_user")
                .email("old@example.com")
                .build();
    }

    // ========== ensureLocalUser() 测试 ==========

    @Test
    @Order(1)
    @DisplayName("测试1：ensureLocalUser - 用户不存在时创建")
    void ensureLocalUser_CreateNew() {
        when(userRepository.findByUsername("new_user")).thenReturn(Optional.empty());

        User newUser = User.builder()
                .username("new_user")
                .email("new@example.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userSyncService.ensureLocalUser("new_user", "new@example.com");

        assertThat(result.getUsername()).isEqualTo("new_user");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @Order(2)
    @DisplayName("测试2：ensureLocalUser - 用户存在但Email不同则更新")
    void ensureLocalUser_UpdateEmail() {
        when(userRepository.findByUsername("old_user")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userSyncService.ensureLocalUser("old_user", "new@example.com");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    @Order(3)
    @DisplayName("测试3：ensureLocalUser - 用户存在且信息一致，无需更新")
    void ensureLocalUser_NoChange() {
        when(userRepository.findByUsername("old_user")).thenReturn(Optional.of(existingUser));

        User result = userSyncService.ensureLocalUser("old_user", "old@example.com");

        assertThat(result).isSameAs(existingUser);
        verify(userRepository, never()).save(any());
    }

    // ========== ensureFromJwt() 测试 ==========

    private Jwt buildJwt(Map<String, Object> claims) {
        return new Jwt(
                "fake-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }

    @Test
    @Order(10)
    @DisplayName("测试10：ensureFromJwt - 新用户创建成功")
    void ensureFromJwt_CreateNew() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "kc-999",
                "preferred_username", "new_user",
                "email", "new@example.com"
        ));

        when(userRepository.findByKcUserId("kc-999")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userSyncService.ensureFromJwt(jwt);

        assertThat(result.getKcUserId()).isEqualTo("kc-999");
        assertThat(result.getUsername()).isEqualTo("new_user");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @Order(11)
    @DisplayName("测试11：ensureFromJwt - 已存在用户但用户名和邮箱变化时更新")
    void ensureFromJwt_UpdateExisting() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "kc-123",
                "preferred_username", "updated_user",
                "email", "new_email@example.com"
        ));

        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userSyncService.ensureFromJwt(jwt);

        assertThat(result.getUsername()).isEqualTo("updated_user");
        assertThat(result.getEmail()).isEqualTo("new_email@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    @Order(12)
    @DisplayName("测试12：ensureFromJwt - 已存在用户但无变更时不更新")
    void ensureFromJwt_NoChange() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "kc-123",
                "preferred_username", "old_user",
                "email", "old@example.com"
        ));

        when(userRepository.findByKcUserId("kc-123")).thenReturn(Optional.of(existingUser));

        User result = userSyncService.ensureFromJwt(jwt);

        assertThat(result).isSameAs(existingUser);
        verify(userRepository, never()).save(any());
    }

    @Test
    @Order(13)
    @DisplayName("测试13：ensureFromJwt - DataIntegrityViolationException 回退读取已有记录")
    void ensureFromJwt_DataIntegrityRecovery() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "kc-777",
                "preferred_username", "conflict_user",
                "email", "conflict@example.com"
        ));

        when(userRepository.findByKcUserId("kc-777")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Unique violation"));

        User result = userSyncService.ensureFromJwt(jwt);

        assertThat(result).isEqualTo(existingUser);
        verify(userRepository, times(2)).findByKcUserId("kc-777");
    }

    @Test
    @Order(14)
    @DisplayName("测试14：ensureFromJwt - 缺少sub字段抛出异常")
    void ensureFromJwt_MissingSub() {
        Jwt jwt = buildJwt(Map.of(
                "preferred_username", "anonymous",
                "email", "anon@example.com"
        ));

        assertThatThrownBy(() -> userSyncService.ensureFromJwt(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT Subject is required");

        verify(userRepository, never()).findByKcUserId(any());
    }
}
