package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotSessionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailStaticInfo;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.dto.response.GroupRecommendResponse;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.global.redis.RedisCacheService;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GroupQueryService 테스트")
class GroupQueryServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupValidator groupValidator;

    @Mock
    private GroupParticipantValidator groupParticipantValidator;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private AiGroupService aiGroupService;

    @Mock
    private GroupCacheService groupCacheService;

    @InjectMocks
    private GroupQueryService groupQueryService;

    @Test
    @DisplayName("[성공] 카테고리별 그룹 조회")
    void getGroups_ByCategory_Success() {
        // given
        GroupSearchRequest request = new GroupSearchRequest();
        request.setCategory("스포츠");
        Pageable pageable = PageRequest.of(0, 10);

        Group mockGroup = createMockGroup(1L, "축구 모임", "스포츠");
        Page<Long> groupIdPage = new PageImpl<>(List.of(1L), pageable, 1);

        // 캐시 미스 시나리오
        given(redisCacheService.get(anyString(), eq(GroupListResponse.class)))
            .willReturn(Optional.empty());

        given(groupRepository.findGroupIdsByCategoryAndStatusNot("스포츠", GroupStatus.DISBANDED, pageable))
            .willReturn(groupIdPage);
        given(groupRepository.findGroupsWithTagsByIds(List.of(1L)))
            .willReturn(List.of(mockGroup));

        // when
        GroupListResponse result = groupQueryService.getGroups(request, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.groups()).hasSize(1);
        verify(groupRepository).findGroupIdsByCategoryAndStatusNot("스포츠", GroupStatus.DISBANDED, pageable);
        verify(groupRepository).findGroupsWithTagsByIds(List.of(1L));
        verify(redisCacheService).set(anyString(), any(GroupListResponse.class), any());
    }

    @Test
    @DisplayName("[성공] 키워드로 그룹 검색")
    void getGroups_ByKeyword_Success() {
        // given
        GroupSearchRequest request = new GroupSearchRequest();
        request.setKeyword("축구");
        Pageable pageable = PageRequest.of(0, 10);

        Group mockGroup = createMockGroup(1L, "축구 모임", "스포츠");
        Page<Long> groupIdPage = new PageImpl<>(List.of(1L), pageable, 1);

        // 캐시 미스 시나리오
        given(redisCacheService.get(anyString(), eq(GroupListResponse.class)))
            .willReturn(Optional.empty());

        given(groupRepository.searchGroupIdsWithKeywordOnly("축구", pageable))
            .willReturn(groupIdPage);
        given(groupRepository.findGroupsWithTagsByIds(List.of(1L)))
            .willReturn(List.of(mockGroup));

        // when
        GroupListResponse result = groupQueryService.getGroups(request, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.groups()).hasSize(1);
        verify(groupRepository).searchGroupIdsWithKeywordOnly("축구", pageable);
        verify(groupRepository).findGroupsWithTagsByIds(List.of(1L));
        verify(redisCacheService).set(anyString(), any(GroupListResponse.class), any());
    }

    @Test
    @DisplayName("[성공] 전체 그룹 조회")
    void getGroups_All_Success() {
        // given
        GroupSearchRequest request = new GroupSearchRequest();
        Pageable pageable = PageRequest.of(0, 10);

        Group mockGroup = createMockGroup(1L, "모임", "스포츠");
        Page<Long> groupIdPage = new PageImpl<>(List.of(1L), pageable, 1);

        // 캐시 미스 시나리오
        given(redisCacheService.get(anyString(), eq(GroupListResponse.class)))
            .willReturn(Optional.empty());

        given(groupRepository.findGroupIdsByStatusNot(pageable))
            .willReturn(groupIdPage);
        given(groupRepository.findGroupsWithTagsByIds(List.of(1L)))
            .willReturn(List.of(mockGroup));

        // when
        GroupListResponse result = groupQueryService.getGroups(request, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.groups()).hasSize(1);
        verify(groupRepository).findGroupIdsByStatusNot(pageable);
        verify(groupRepository).findGroupsWithTagsByIds(List.of(1L));
        verify(redisCacheService).set(anyString(), any(GroupListResponse.class), any());
    }

    @Test
    @DisplayName("[성공] 빈 키워드로 그룹 검색")
    void getGroups_EmptyKeyword_Success() {
        // given
        GroupSearchRequest request = new GroupSearchRequest();
        request.setKeyword("   "); // 빈 문자열
        Pageable pageable = PageRequest.of(0, 10);

        Group mockGroup = createMockGroup(1L, "모임", "스포츠");
        Page<Long> groupIdPage = new PageImpl<>(List.of(1L), pageable, 1);

        // 캐시 미스 시나리오
        given(redisCacheService.get(anyString(), eq(GroupListResponse.class)))
            .willReturn(Optional.empty());

        given(groupRepository.findGroupIdsByStatusNot(pageable))
            .willReturn(groupIdPage);
        given(groupRepository.findGroupsWithTagsByIds(List.of(1L)))
            .willReturn(List.of(mockGroup));

        // when
        GroupListResponse result = groupQueryService.getGroups(request, pageable);

        // then
        assertThat(result).isNotNull();
        verify(groupRepository).findGroupIdsByStatusNot(pageable);
        verify(redisCacheService).set(anyString(), any(GroupListResponse.class), any());
    }

    @Test
    @DisplayName("[성공] 캐시 히트 - 그룹 조회")
    void getGroups_CacheHit_Success() {
        // given
        GroupSearchRequest request = new GroupSearchRequest();
        Pageable pageable = PageRequest.of(0, 10);

        GroupListResponse cachedResponse = GroupListResponse.from(
            new PageImpl<>(List.of(), pageable, 0)
        );

        // 캐시 히트 시나리오
        given(redisCacheService.get(anyString(), eq(GroupListResponse.class)))
            .willReturn(Optional.of(cachedResponse));

        // when
        GroupListResponse result = groupQueryService.getGroups(request, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cachedResponse);
        verify(redisCacheService).get(anyString(), eq(GroupListResponse.class));
        // DB 조회가 일어나지 않아야 함
        verify(groupRepository, org.mockito.Mockito.never()).findGroupIdsByStatusNot(any());
    }

    @Test
    @DisplayName("[성공] 그룹 상세 조회")
    void detailGroup_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        TestReflectionUtils.setField(group, "id", 1L);
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        GroupDetailStaticInfo staticInfo = new GroupDetailStaticInfo(
            "테스트 그룹", "스포츠", "요약", "설명", "계획", "서울", 5,10, "image.jpg", List.of("태그1"), "소유자"
        );

        given(groupCacheService.getGroupDetailStatic(group.getId())).willReturn(staticInfo);
        given(groupValidator.findByIdOrThrow(group.getId())).willReturn(group);
        given(groupParticipantValidator.checkUserRole(customUserDetails, group.getId())).willReturn(Role.MEMBER);

        // when
        GroupDetailResponse result = groupQueryService.detailGroup(group.getId(), customUserDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("테스트 그룹");
        verify(groupCacheService).getGroupDetailStatic(group.getId());
        verify(groupValidator).findByIdOrThrow(group.getId());
        verify(groupParticipantValidator).checkUserRole(customUserDetails, group.getId());
    }

    @Test
    @DisplayName("[성공] 챗봇 세션 조회 - 세션 존재")
    void getChatbotSession_SessionExists_Success() throws Exception {
        // given
        Long userId = 100L;
        String sessionId = "session123";

        String jsonData = """
            {
                "messages": [
                    {
                        "role": "user",
                        "text": "안녕하세요",
                        "options": ["옵션1", "옵션2"]
                    }
                ]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonData);

        given(redisCacheService.get(anyString(), any(Class.class))).willReturn(Optional.of(jsonNode));

        // when
        GroupChatbotSessionResponse result = groupQueryService.getChatbotSession(userId, sessionId);

        // then
        assertThat(result).isNotNull();
        verify(redisCacheService).get(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("[성공] 챗봇 세션 조회 - 세션 없음")
    void getChatbotSession_SessionNotExists_ReturnNull() {
        // given
        Long userId = 100L;
        String sessionId = "session123";

        given(redisCacheService.get(anyString(), any(Class.class))).willReturn(Optional.empty());

        // when
        GroupChatbotSessionResponse result = groupQueryService.getChatbotSession(userId, sessionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.messages()).isNull();
        verify(redisCacheService).get(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("[실패] 그룹 추천 - 빈 메시지")
    void recommendGroup_EmptyMessages_ThrowException() {
        // given
        Long userId = 100L;
        GroupChatbotSessionResponse session = new GroupChatbotSessionResponse(
            List.of()
        );
        String sessionId = "session123";

        // when & then
        assertThatThrownBy(() -> groupQueryService.recommendGroup(userId, session, sessionId))
            .isInstanceOf(GroupException.class)
            .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.CHAT_SESSION_NOT_FOUND);
    }

    @Test
    @DisplayName("[성공] 그룹 추천")
    void recommendGroup_Success() {
        // given
        Long userId = 100L;
        String sessionId = "session123";

        List<GroupChatbotSessionResponse.Message> messages = List.of(
            new GroupChatbotSessionResponse.Message("user", "스포츠 모임 추천해주세요", List.of())
        );
        GroupChatbotSessionResponse session = new GroupChatbotSessionResponse(
            messages
        );

        GroupAiRecommendResponse aiResponse = new GroupAiRecommendResponse(1L, "추천 이유", null);
        Group mockGroup = createMockGroup(1L, "축구 모임", "스포츠");

        given(aiGroupService.recommendGroup(any(), anyString())).willReturn(aiResponse);
        given(groupValidator.findByIdOrThrow(1L)).willReturn(mockGroup);

        // when
        GroupRecommendResponse result = groupQueryService.recommendGroup(userId, session, sessionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.reason()).isEqualTo("추천 이유");
        verify(aiGroupService).recommendGroup(any(), anyString());
        verify(groupValidator).findByIdOrThrow(1L);
    }

    // 헬퍼 메서드들
    private Group createMockGroup(Long id, String name, String category) {
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(group.getName()).willReturn(name);
        given(group.getCategory()).willReturn(category);
        given(group.getStatus()).willReturn(GroupStatus.ACTIVE);
        given(group.getCurrentUserCount()).willReturn(5);
        return group;
    }

    private CustomUserDetails createCustomUserDetails(Long userId) {
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .nickname("testuser")
            .build();
        return new CustomUserDetails(user);
    }
}
