package com.ntdoc.notangdoccore.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntdoc.notangdoccore.controller.TeamMemberController;
import com.ntdoc.notangdoccore.dto.common.ApiResponse;
import com.ntdoc.notangdoccore.dto.team.TeamMemberAddRequest;
import com.ntdoc.notangdoccore.dto.team.TeamMemberListResponse;
import com.ntdoc.notangdoccore.dto.team.TeamMemberResponse;
import com.ntdoc.notangdoccore.dto.team.TeamMemberUpdateRequest;
import com.ntdoc.notangdoccore.entity.Team;
import com.ntdoc.notangdoccore.entity.TeamMember;
import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.service.TeamMemberService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TeamMemberController.class})
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamMemberControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamMemberService teamMemberService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private User testOwner;
    private User testMember;
    private Team testTeam;
    private TeamMember ownerTeamMember;
    private TeamMember normalTeamMember;

    @BeforeEach
    void setUp() {
        log.info("=== Test Begin ===");
        reset(teamMemberService, teamService);

        // 创建测试用户
        testOwner = User.builder()
                .id(1L)
                .kcUserId("owner-123")
                .username("owner_user")
                .email("owner@example.com")
                .build();

        testMember = User.builder()
                .id(2L)
                .kcUserId("member-456")
                .username("member_user")
                .email("member@example.com")
                .build();

        // 创建测试团队
        testTeam = Team.builder()
                .id(100L)
                .name("Test Team")
                .description("Test Description")
                .owner(testOwner)
                .status(Team.TeamStatus.ACTIVE)
                .memberCount(2)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 创建团队成员
        ownerTeamMember = TeamMember.builder()
                .id(1L)
                .team(testTeam)
                .user(testOwner)
                .role(TeamMember.TeamRole.OWNER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        normalTeamMember = TeamMember.builder()
                .id(2L)
                .team(testTeam)
                .user(testMember)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        log.info("=== Test End ===\n");
    }

    @Test
    @Order(1)
    @DisplayName("添加成员 - 成功")
    void addMember_Success() throws Exception {
        log.info("Test: Add Member - Success");

        TeamMemberAddRequest request = TeamMemberAddRequest.builder()
                .userKcId("new-user-789")
                .role("MEMBER")
                .build();

        User newUser = User.builder()
                .id(3L)
                .kcUserId("new-user-789")
                .username("newuser")
                .build();

        TeamMember newMember = TeamMember.builder()
                .id(3L)
                .team(testTeam)
                .user(newUser)
                .role(TeamMember.TeamRole.MEMBER)
                .status(TeamMember.MemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        when(teamMemberService.addMember(eq(100L), eq("new-user-789"), eq("MEMBER"), eq("owner-123")))
                .thenReturn(newMember);

        MvcResult result = mockMvc.perform(
                        post("/api/v1/teams/100/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成员添加成功"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andReturn();

        ApiResponse<TeamMemberResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(response.isSuccess()).isTrue();
        verify(teamMemberService, times(1)).addMember(eq(100L), eq("new-user-789"), eq("MEMBER"), eq("owner-123"));
    }

    @Test
    @Order(2)
    @DisplayName("添加成员 - 无权限")
    void addMember_Forbidden() throws Exception {
        log.info("Test: Add Member - Forbidden");

        TeamMemberAddRequest request = TeamMemberAddRequest.builder()
                .userKcId("new-user-789")
                .role("MEMBER")
                .build();

        when(teamMemberService.addMember(eq(100L), eq("new-user-789"), eq("MEMBER"), eq("member-456")))
                .thenThrow(new SecurityException("只有团队拥有者或管理员可以添加成员"));

        mockMvc.perform(
                        post("/api/v1/teams/100/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "member-456")
                                ))
                )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只有团队拥有者或管理员可以添加成员"));
    }

    @Test
    @Order(3)
    @DisplayName("添加成员 - 用户已存在")
    void addMember_AlreadyExists() throws Exception {
        log.info("Test: Add Member - Already Exists");

        TeamMemberAddRequest request = TeamMemberAddRequest.builder()
                .userKcId("member-456")
                .role("MEMBER")
                .build();

        when(teamMemberService.addMember(eq(100L), eq("member-456"), eq("MEMBER"), eq("owner-123")))
                .thenThrow(new IllegalArgumentException("用户已经是团队成员"));

        mockMvc.perform(
                        post("/api/v1/teams/100/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户已经是团队成员"));
    }

    @Test
    @Order(10)
    @DisplayName("获取团队成员列表 - 成功（仅活跃成员）")
    void getTeamMembers_ActiveOnly_Success() throws Exception {
        log.info("Test: Get Team Members - Active Only Success");

        List<TeamMember> activeMembers = Arrays.asList(ownerTeamMember, normalTeamMember);

        when(teamMemberService.getActiveTeamMembers(eq(100L), eq("owner-123")))
                .thenReturn(activeMembers);

        when(teamService.getTeamById(100L))
                .thenReturn(testTeam);

        mockMvc.perform(
                        get("/api/v1/teams/100/members")
                                .param("activeOnly", "true")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取成员列表成功"))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.members.length()").value(2))
                .andExpect(jsonPath("$.data.teamName").value("Test Team"))
                .andExpect(jsonPath("$.data.totalMembers").value(2));

        verify(teamMemberService).getActiveTeamMembers(eq(100L), eq("owner-123"));
        verify(teamService).getTeamById(100L);
    }

    @Test
    @Order(11)
    @DisplayName("获取团队成员列表 - 所有成员")
    void getTeamMembers_AllMembers_Success() throws Exception {
        log.info("Test: Get Team Members - All Members Success");

        List<TeamMember> allMembers = Arrays.asList(ownerTeamMember, normalTeamMember);

        when(teamMemberService.getTeamMembers(eq(100L), eq("owner-123")))
                .thenReturn(allMembers);

        when(teamService.getTeamById(100L))
                .thenReturn(testTeam);

        mockMvc.perform(
                        get("/api/v1/teams/100/members")
                                .param("activeOnly", "false")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.totalMembers").value(2));

        verify(teamMemberService).getTeamMembers(eq(100L), eq("owner-123"));
        verify(teamMemberService, never()).getActiveTeamMembers(anyLong(), anyString());
    }

    @Test
    @Order(12)
    @DisplayName("获取团队成员列表 - 无权限")
    void getTeamMembers_Forbidden() throws Exception {
        log.info("Test: Get Team Members - Forbidden");

        when(teamMemberService.getActiveTeamMembers(eq(100L), eq("outsider-999")))
                .thenThrow(new SecurityException("只有团队成员可以查看成员列表"));

        mockMvc.perform(
                        get("/api/v1/teams/100/members")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "outsider-999")
                                ))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只有团队成员可以查看成员列表"));
    }

    @Test
    @Order(20)
    @DisplayName("更新成员角色 - 成功")
    void updateMemberRole_Success() throws Exception {
        log.info("Test: Update Member Role - Success");

        TeamMemberUpdateRequest request = TeamMemberUpdateRequest.builder()
                .role("ADMIN")
                .build();

        TeamMember updatedMember = TeamMember.builder()
                .id(2L)
                .team(testTeam)
                .user(testMember)
                .role(TeamMember.TeamRole.ADMIN)  // 角色已更新
                .status(TeamMember.MemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();

        when(teamMemberService.updateMemberRole(eq(100L), eq(2L), eq("ADMIN"), eq("owner-123")))
                .thenReturn(updatedMember);

        mockMvc.perform(
                        put("/api/v1/teams/100/members/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成员角色更新成功"))
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        verify(teamMemberService).updateMemberRole(eq(100L), eq(2L), eq("ADMIN"), eq("owner-123"));
    }

    @Test
    @Order(21)
    @DisplayName("更新成员角色 - 无权限（非OWNER）")
    void updateMemberRole_Forbidden() throws Exception {
        log.info("Test: Update Member Role - Forbidden");

        TeamMemberUpdateRequest request = TeamMemberUpdateRequest.builder()
                .role("ADMIN")
                .build();

        when(teamMemberService.updateMemberRole(eq(100L), eq(2L), eq("ADMIN"), eq("member-456")))
                .thenThrow(new SecurityException("只有团队拥有者可以修改成员角色"));

        mockMvc.perform(
                        put("/api/v1/teams/100/members/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "member-456")
                                ))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只有团队拥有者可以修改成员角色"));
    }

    @Test
    @Order(22)
    @DisplayName("更新成员角色 - 不能设置为OWNER")
    void updateMemberRole_CannotSetOwner() throws Exception {
        log.info("Test: Update Member Role - Cannot Set Owner");

        TeamMemberUpdateRequest request = TeamMemberUpdateRequest.builder()
                .role("OWNER")
                .build();

        when(teamMemberService.updateMemberRole(eq(100L), eq(2L), eq("OWNER"), eq("owner-123")))
                .thenThrow(new IllegalArgumentException("不能将成员设置为拥有者"));

        mockMvc.perform(
                        put("/api/v1/teams/100/members/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能将成员设置为拥有者"));
    }

    @Test
    @Order(30)
    @DisplayName("移除成员 - 成功")
    void removeMember_Success() throws Exception {
        log.info("Test: Remove Member - Success");

        doNothing().when(teamMemberService).removeMember(eq(100L), eq(2L), eq("owner-123"));

        mockMvc.perform(
                        delete("/api/v1/teams/100/members/2")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成员移除成功"));

        verify(teamMemberService).removeMember(eq(100L), eq(2L), eq("owner-123"));
    }

    @Test
    @Order(31)
    @DisplayName("移除成员 - 无权限")
    void removeMember_Forbidden() throws Exception {
        log.info("Test: Remove Member - Forbidden");

        doThrow(new SecurityException("只有团队拥有者或管理员可以移除成员"))
                .when(teamMemberService).removeMember(eq(100L), eq(2L), eq("member-456"));

        mockMvc.perform(
                        delete("/api/v1/teams/100/members/2")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "member-456")
                                ))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("只有团队拥有者或管理员可以移除成员"));
    }

    @Test
    @Order(32)
    @DisplayName("移除成员 - 不能移除OWNER")
    void removeMember_CannotRemoveOwner() throws Exception {
        log.info("Test: Remove Member - Cannot Remove Owner");

        doThrow(new IllegalArgumentException("不能移除团队拥有者"))
                .when(teamMemberService).removeMember(eq(100L), eq(1L), eq("owner-123"));

        mockMvc.perform(
                        delete("/api/v1/teams/100/members/1")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能移除团队拥有者"));
    }

    @Test
    @Order(33)
    @DisplayName("移除成员 - 成员不存在")
    void removeMember_NotFound() throws Exception {
        log.info("Test: Remove Member - Not Found");

        doThrow(new RuntimeException("成员记录不存在: 999"))
                .when(teamMemberService).removeMember(eq(100L), eq(999L), eq("owner-123"));

        mockMvc.perform(
                        delete("/api/v1/teams/100/members/999")
                                .with(jwt().jwt(builder -> builder
                                        .claim("sub", "owner-123")
                                ))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(40)
    @DisplayName("未认证访问 - 应该被拒绝")
    void unauthenticated_Forbidden() throws Exception {
        log.info("Test: Unauthenticated - Forbidden");

        TeamMemberAddRequest request = TeamMemberAddRequest.builder()
                .userKcId("new-user-789")
                .role("MEMBER")
                .build();

        mockMvc.perform(
                        post("/api/v1/teams/100/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                        // 不添加 JWT
                )
                .andExpect(status().isForbidden());

        verify(teamMemberService, never()).addMember(anyLong(), anyString(), anyString(), anyString());
    }
}

