package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;
import kr.ai.nemo.domain.group.repository.GroupTagRepository;
import kr.ai.nemo.domain.group.repository.TagRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // Strict stubbing 완화
@DisplayName("GroupTagService 테스트")
class GroupTagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private GroupTagRepository groupTagRepository;

    @InjectMocks
    private GroupTagService groupTagService;

    @Test
    @DisplayName("[성공] 기존 태그로 그룹에 태그 할당")
    void assignTags_WithExistingTags_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .build();

        List<String> tagNames = Arrays.asList("운동", "헬스");
        Tag exerciseTag = new Tag("운동");
        Tag healthTag = new Tag("헬스");

        given(tagRepository.findByName("운동")).willReturn(Optional.of(exerciseTag));
        given(tagRepository.findByName("헬스")).willReturn(Optional.of(healthTag));
        given(groupTagRepository.save(any(GroupTag.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        groupTagService.assignTags(group, tagNames);

        // then
        assertThat(group.getGroupTags()).hasSize(2);
        verify(tagRepository).findByName("운동");
        verify(tagRepository).findByName("헬스");
        verify(groupTagRepository, times(2)).save(any(GroupTag.class));
    }

    @Test
    @DisplayName("[성공] 새로운 태그 생성 후 그룹에 태그 할당")
    void assignTags_WithNewTags_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .build();

        List<String> tagNames = Arrays.asList("새로운태그", "또다른태그");
        Tag newTag1 = new Tag("새로운태그");
        Tag newTag2 = new Tag("또다른태그");

        given(tagRepository.findByName("새로운태그")).willReturn(Optional.empty());
        given(tagRepository.findByName("또다른태그")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class)))
                .willReturn(newTag1)
                .willReturn(newTag2);
        given(groupTagRepository.save(any(GroupTag.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        group.getGroupTags().clear();

        // when
        groupTagService.assignTags(group, tagNames);

        // then
        assertThat(group.getGroupTags()).hasSize(2);
        verify(tagRepository).findByName("새로운태그");
        verify(tagRepository).findByName("또다른태그");
        verify(tagRepository, times(2)).save(any(Tag.class));
        verify(groupTagRepository, times(2)).save(any(GroupTag.class));
    }

    @Test
    @DisplayName("[성공] 기존 태그와 새로운 태그 혼합 할당")
    void assignTags_MixedExistingAndNewTags_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .build();

        List<String> tagNames = Arrays.asList("운동", "새로운태그");
        Tag existingTag = new Tag("운동");
        Tag newTag = new Tag("새로운태그");

        given(tagRepository.findByName("운동")).willReturn(Optional.of(existingTag));
        given(tagRepository.findByName("새로운태그")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willReturn(newTag);
        given(groupTagRepository.save(any(GroupTag.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        group.getGroupTags().clear();

        // when
        groupTagService.assignTags(group, tagNames);

        // then
        assertThat(group.getGroupTags()).hasSize(2);
        verify(tagRepository).findByName("운동");
        verify(tagRepository).findByName("새로운태그");
        verify(tagRepository, times(1)).save(any(Tag.class)); // 새로운 태그만 저장
        verify(groupTagRepository, times(2)).save(any(GroupTag.class));
    }

    @Test
    @DisplayName("[성공] 빈 태그 리스트로 할당")
    void assignTags_EmptyTagList_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .build();

        List<String> emptyTagNames = Arrays.asList();

        // when
        groupTagService.assignTags(group, emptyTagNames);

        // then
        assertThat(group.getGroupTags()).isEmpty();
        verify(tagRepository, times(0)).findByName(anyString());
        verify(tagRepository, times(0)).save(any(Tag.class));
        verify(groupTagRepository, times(0)).save(any(GroupTag.class));
    }

    @Test
    @DisplayName("[성공] 그룹 ID로 태그명 조회")
    void getTagNamesByGroupId_Success() {
        // given
        Long groupId = 1L;
        Tag tag1 = new Tag("운동");
        Tag tag2 = new Tag("헬스");
        
        GroupTag groupTag1 = GroupTag.builder()
                .tag(tag1)
                .build();
        GroupTag groupTag2 = GroupTag.builder()
                .tag(tag2)
                .build();

        List<GroupTag> groupTags = Arrays.asList(groupTag1, groupTag2);
        given(groupTagRepository.findByGroupId(groupId)).willReturn(groupTags);

        // when
        List<String> result = groupTagService.getTagNamesByGroupId(groupId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("운동", "헬스");
        verify(groupTagRepository).findByGroupId(groupId);
    }

    @Test
    @DisplayName("[성공] 태그가 없는 그룹의 태그명 조회")
    void getTagNamesByGroupId_NoTags_Success() {
        // given
        Long groupId = 1L;
        given(groupTagRepository.findByGroupId(groupId)).willReturn(Arrays.asList());

        // when
        List<String> result = groupTagService.getTagNamesByGroupId(groupId);

        // then
        assertThat(result).isEmpty();
        verify(groupTagRepository).findByGroupId(groupId);
    }
}
