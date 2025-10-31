package com.ntdoc.notangdoccore.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntdoc.notangdoccore.controller.TeamController;
import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.team.TeamCreateRequest;
import com.ntdoc.notangdoccore.dto.team.TeamResponse;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.service.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { TeamController.class })
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private User testUser;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        log.info("=== Test Begin ===");
        reset(teamService);

        // 创建测试用户
        testUser = User.builder()
                .id(1L)
                .kcUserId("user-123")
                .username("test_user")
                .email("test@example.com")
                .build();

        // 创建测试团队
        testTeam = Team.builder()
                .id(100L)
                .name("Test Team")
                .description("Test Description")
                .owner(testUser)
                .status(Team.TeamStatus.ACTIVE)  // ✅ 使用 status 而不是 isActive
                .memberCount(1)  // ✅ 添加 memberCount
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        log.info("=== Test End ===\n");
    }

    private Team createMockTeam(Long id, String name, User owner, Team.TeamStatus status) {
        return Team.builder()
                .id(id)
                .name(name)
                .description("Test Description")
                .owner(owner)
                .status(status)
                .memberCount(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("创建团队 - 成功")
    void createTeam_Success() throws Exception {
        log.info("Test: Create Team - Success");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("New Team")
                .description("New Team Description")
                .build();

        when(teamService.createTeam(any(TeamCreateRequest.class), eq("user-123")))
                .thenReturn(testTeam);

        MvcResult result = mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("团队创建成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.teamId").value(100L))
                .andExpect(jsonPath("$.data.name").value("Test Team"))
                .andReturn();

        ApiResponse<TeamResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getTeamId()).isEqualTo(100L);

        verify(teamService, times(1)).createTeam(any(TeamCreateRequest.class), eq("user-123"));
    }

    @Test
    @Order(2)
    @DisplayName("创建团队 - 参数错误")
    void createTeam_InvalidArgument() throws Exception {
        log.info("Test: Create Team - Invalid Argument");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("Invalid Team")
                .build();

        when(teamService.createTeam(any(TeamCreateRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("团队名称已存在"));

        mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("团队名称已存在"));
    }

    @Test
    @Order(3)
    @DisplayName("创建团队 - 服务器错误")
    void createTeam_ServerError() throws Exception {
        log.info("Test: Create Team - Server Error");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("New Team")
                .build();

        when(teamService.createTeam(any(TeamCreateRequest.class), anyString()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("团队创建失败: 数据库连接失败"));
    }

    @Test
    @Order(4)
    @DisplayName("创建团队 - 未认证")
    void createTeam_Unauthorized() throws Exception {
        log.info("Test: Create Team - Unauthorized");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("New Team")
                .build();

        mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                        // 不添加 JWT
                )
                .andExpect(status().isForbidden());

        verify(teamService, never()).createTeam(any(), anyString());
    }

    @Test
    @Order(10)
    @DisplayName("获取用户团队 - 所有活跃团队")
    void getUserTeams_ActiveOnly() throws Exception {
        log.info("Test: Get User Teams - Active Only");

        Team team1 = createMockTeam(1L, "Team 1", testUser, Team.TeamStatus.ACTIVE);
        Team team2 = createMockTeam(2L, "Team 2", testUser, Team.TeamStatus.ACTIVE);

        when(teamService.getUserActiveTeams("user-123"))
                .thenReturn(Arrays.asList(team1, team2));

        mockMvc.perform(
                        get("/api/v1/teams")
                                .param("activeOnly", "true")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取团队列表成功"))
                .andExpect(jsonPath("$.data.teams").isArray())
                .andExpect(jsonPath("$.data.teams.length()").value(2))
                .andExpect(jsonPath("$.data.teams[0].teamId").value(1L))
                .andExpect(jsonPath("$.data.teams[1].teamId").value(2L));

        verify(teamService).getUserActiveTeams("user-123");
        verify(teamService, never()).getUserOwnedTeams(anyString());
    }

    @Test
    @Order(11)
    @DisplayName("获取用户团队 - 所有团队（包括非活跃）")
    void getUserTeams_AllTeams() throws Exception {
        log.info("Test: Get User Teams - All Teams");

        Team activeTeam = createMockTeam(1L, "Active Team", testUser, Team.TeamStatus.ACTIVE);
        Team archivedTeam = createMockTeam(2L, "Archived Team", testUser, Team.TeamStatus.ARCHIVED);

        when(teamService.getUserOwnedTeams("user-123"))
                .thenReturn(Arrays.asList(activeTeam, archivedTeam));

        mockMvc.perform(
                        get("/api/v1/teams")
                                .param("activeOnly", "false")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teams").isArray())
                .andExpect(jsonPath("$.data.teams.length()").value(2));

        verify(teamService).getUserOwnedTeams("user-123");
        verify(teamService, never()).getUserActiveTeams(anyString());
    }

    @Test
    @Order(12)
    @DisplayName("获取用户团队 - 默认获取活跃团队")
    void getUserTeams_DefaultActiveOnly() throws Exception {
        log.info("Test: Get User Teams - Default Active Only");

        when(teamService.getUserActiveTeams("user-123"))
                .thenReturn(List.of(testTeam));

        // 不传 activeOnly 参数，应该默认为 true
        mockMvc.perform(
                        get("/api/v1/teams")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teams").isArray());

        verify(teamService).getUserActiveTeams("user-123");
    }

    @Test
    @Order(13)
    @DisplayName("获取用户团队 - 空列表")
    void getUserTeams_EmptyList() throws Exception {
        log.info("Test: Get User Teams - Empty List");

        when(teamService.getUserActiveTeams("user-123"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/teams")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teams").isArray())
                .andExpect(jsonPath("$.data.teams.length()").value(0));
    }

    @Test
    @Order(14)
    @DisplayName("获取用户团队 - 服务器错误")
    void getUserTeams_ServerError() throws Exception {
        log.info("Test: Get User Teams - Server Error");

        when(teamService.getUserActiveTeams(anyString()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(
                        get("/api/v1/teams")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(20)
    @DisplayName("获取团队详情 - 成功")
    void getTeamById_Success() throws Exception {
        log.info("Test: Get Team By ID - Success");

        when(teamService.getTeamById(100L))
                .thenReturn(testTeam);

        mockMvc.perform(
                        get("/api/v1/teams/100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取团队信息成功"))
                .andExpect(jsonPath("$.data.teamId").value(100L))
                .andExpect(jsonPath("$.data.name").value("Test Team"));

        verify(teamService).getTeamById(100L);
    }

    @Test
    @Order(21)
    @DisplayName("获取团队详情 - 团队不存在")
    void getTeamById_NotFound() throws Exception {
        log.info("Test: Get Team By ID - Not Found");

        when(teamService.getTeamById(999L))
                .thenThrow(new RuntimeException("Team not found"));

        mockMvc.perform(
                        get("/api/v1/teams/999")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("团队不存在"));
    }


    @Test
    @Order(30)
    @DisplayName("更新团队 - 成功")
    void updateTeam_Success() throws Exception {
        log.info("Test: Update Team - Success");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("Updated Team")
                .description("Updated Description")
                .build();

        Team updatedTeam = Team.builder()
                .id(100L)
                .name("Updated Team")
                .description("Updated Description")
                .owner(testUser)
                .status(Team.TeamStatus.ACTIVE)
                .memberCount(5)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(teamService.updateTeam(eq(100L), any(TeamCreateRequest.class), eq("user-123")))
                .thenReturn(updatedTeam);

        mockMvc.perform(
                        put("/api/v1/teams/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("团队更新成功"))
                .andExpect(jsonPath("$.data.teamId").value(100L))
                .andExpect(jsonPath("$.data.name").value("Updated Team"));

        verify(teamService).updateTeam(eq(100L), any(TeamCreateRequest.class), eq("user-123"));
    }

    @Test
    @Order(31)
    @DisplayName("更新团队 - 无权限")
    void updateTeam_Forbidden() throws Exception {
        log.info("Test: Update Team - Forbidden");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("Updated Team")
                .build();

        when(teamService.updateTeam(eq(100L), any(TeamCreateRequest.class), eq("user-456")))
                .thenThrow(new SecurityException("只有团队拥有者可以更新团队"));

        mockMvc.perform(
                        put("/api/v1/teams/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-456")
                                ))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只有团队拥有者可以更新团队"));
    }

    @Test
    @Order(32)
    @DisplayName("更新团队 - 参数错误")
    void updateTeam_InvalidArgument() throws Exception {
        log.info("Test: Update Team - Invalid Argument");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("")  // 空名称
                .build();

        when(teamService.updateTeam(eq(100L), any(TeamCreateRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("团队名称不能为空"));

        mockMvc.perform(
                        put("/api/v1/teams/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(33)
    @DisplayName("更新团队 - 服务器错误")
    void updateTeam_ServerError() throws Exception {
        log.info("Test: Update Team - Server Error");

        TeamCreateRequest request = TeamCreateRequest.builder()
                .name("Updated Team")
                .build();

        when(teamService.updateTeam(eq(100L), any(TeamCreateRequest.class), anyString()))
                .thenThrow(new RuntimeException("数据库错误"));

        mockMvc.perform(
                        put("/api/v1/teams/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(40)
    @DisplayName("删除团队 - 成功")
    void deleteTeam_Success() throws Exception {
        log.info("Test: Delete Team - Success");

        doNothing().when(teamService).deleteTeam(100L, "user-123");

        mockMvc.perform(
                        delete("/api/v1/teams/100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("团队删除成功"));

        verify(teamService).deleteTeam(100L, "user-123");
    }

    @Test
    @Order(41)
    @DisplayName("删除团队 - 无权限")
    void deleteTeam_Forbidden() throws Exception {
        log.info("Test: Delete Team - Forbidden");

        doThrow(new SecurityException("只有团队拥有者可以删除团队"))
                .when(teamService).deleteTeam(100L, "user-456");

        mockMvc.perform(
                        delete("/api/v1/teams/100")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-456")
                                ))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只有团队拥有者可以删除团队"));
    }

    @Test
    @Order(42)
    @DisplayName("删除团队 - 团队不存在")
    void deleteTeam_NotFound() throws Exception {
        log.info("Test: Delete Team - Not Found");

        doThrow(new RuntimeException("Team not found"))
                .when(teamService).deleteTeam(999L, "user-123");

        mockMvc.perform(
                        delete("/api/v1/teams/999")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "user-123")
                                ))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("团队不存在"));
    }
}

