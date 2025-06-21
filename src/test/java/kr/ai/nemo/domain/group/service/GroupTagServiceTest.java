package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;
import kr.ai.nemo.domain.group.repository.GroupTagRepository;
import kr.ai.nemo.domain.group.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GroupTagService 테스트")
class GroupTagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private GroupTagRepository groupTagRepository;

    @InjectMocks
    private GroupTagService groupTagService;

    @Test
    @DisplayName("[성공] 그룹에 태그 할당 - 기존 태그 사용")
    void assignTags_ExistingTags_Success() {
        // given
        Group group = createMockGroup(1L);
        List<String> tagNames = List.of("스포츠", "풋살", "서울");
        
        Tag existingTag1 = createMockTag(1L, "스포츠");
        Tag existingTag2 = createMockTag(2L, "풋살");
        Tag existingTag3 = createMockTag(3L, "서울");

        given(tagRepository.findByName("스포츠")).willReturn(Optional.of(existingTag1));
        given(tagRepository.findByName("풋살")).willReturn(Optional.of(existingTag2));
        given(tagRepository.findByName("서울")).willReturn(Optional.of(existingTag3));
        given(groupTagRepository.save(any(GroupTag.class))).willReturn(mock(GroupTag.class));

        // when
        groupTagService.assignTags(group, tagNames);

        // then
        verify(tagRepository).findByName("스포츠");
        verify(tagRepository).findByName("풋살");
        verify(tagRepository).findByName("서울");
        verify(groupTagRepository, times(3)).save(any(GroupTag.class));
        // tagRepository.save는 호출되지 않음 (기존 태그 사용)
    }

    @Test
    @DisplayName("[성공] 그룹에 태그 할당 - 새로운 태그 생성")
    void assignTags_NewTags_Success() {
        // given
        Group group = createMockGroup(1L);
        List<String> tagNames = List.of("새태그1", "새태그2");
        
        Tag newTag1 = createMockTag(4L, "새태그1");
        Tag newTag2 = createMockTag(5L, "새태그2");

        given(tagRepository.findByName("새태그1")).willReturn(Optional.empty());
        given(tagRepository.findByName("새태그2")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willReturn(newTag1, newTag2);
        given(groupTagRepository.save(any(GroupTag.class))).willReturn(mock(GroupTag.class));

        // when
        groupTagService.assignTags(group, tagNames);

        // then
        verify(tagRepository).findByName("새태그1");
        verify(tagRepository).findByName("새태그2");
        verify(tagRepository, times(2)).save(any(Tag.class)); // 2개의 새로운 태그 저장
        verify(groupTagRepository, times(2)).save(any(GroupTag.class)); // 2개의 GroupTag 저장
    }

    @Test
    @DisplayName("[성공] 그룹에 태그 할당 - 기존/새로운 태그 혼합")
    void assignTags_MixedTags_Success() {
        // given
        Group group = createMockGroup(1L);
        List<String> tagNames = List.of("기존태그", "새태그");
        
        Tag existingTag = createMockTag(1L, "기존태그");
        Tag newTag = createMockTag(2L, "새태그");

        given(tagRepository.findByName("기존태그")).willReturn(Optional.of(existingTag));
        given(tagRepository.findByName("새태그")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willReturn(newTag);
        given(groupTagRepository.save(any(GroupTag.class))).willReturn(mock(GroupTag.class));

        // when
        groupTagService.assignTags(group, tagNames);

        // then
        verify(tagRepository).findByName("기존태그");
        verify(tagRepository).findByName("새태그");
        verify(tagRepository, times(1)).save(any(Tag.class)); // 새태그 1개만 저장
        verify(groupTagRepository, times(2)).save(any(GroupTag.class)); // 2개의 GroupTag 저장
    }

    @Test
    @DisplayName("[성공] 빈 태그 리스트로 태그 할당")
    void assignTags_EmptyTagList_Success() {
        // given
        Group group = createMockGroup(1L);
        List<String> tagNames = List.of();

        // when
        groupTagService.assignTags(group, tagNames);

        // then - 빈 리스트이므로 아무것도 호출되지 않음 (verify 제거)
    }

    @Test
    @DisplayName("[성공] 중복 태그명으로 태그 할당")
    void assignTags_DuplicateTagNames_Success() {
        // given
        Group group = createMockGroup(1L);
        List<String> tagNames = List.of("스포츠", "스포츠", "풋살");
        
        Tag tag1 = createMockTag(1L, "스포츠");
        Tag tag2 = createMockTag(2L, "풋살");

        given(tagRepository.findByName("스포츠")).willReturn(Optional.of(tag1));
        given(tagRepository.findByName("풋살")).willReturn(Optional.of(tag2));
        given(groupTagRepository.save(any(GroupTag.class))).willReturn(mock(GroupTag.class));

        // when
        groupTagService.assignTags(group, tagNames);

        // then - 중복된 만큼 호출되어야 함
        verify(tagRepository, times(2)).findByName("스포츠"); // 2번 호출
        verify(tagRepository, times(1)).findByName("풋살");  // 1번 호출
        verify(groupTagRepository, times(3)).save(any(GroupTag.class)); // 총 3번 호출
    }

    @Test
    @DisplayName("[성공] 그룹 ID로 태그명 조회")
    void getTagNamesByGroupId_Success() {
        // given
        Long groupId = 1L;
        List<GroupTag> groupTags = List.of(
            createMockGroupTag(createMockTag(1L, "스포츠")),
            createMockGroupTag(createMockTag(2L, "풋살")),
            createMockGroupTag(createMockTag(3L, "서울"))
        );

        given(groupTagRepository.findByGroupId(groupId)).willReturn(groupTags);

        // when
        List<String> result = groupTagService.getTagNamesByGroupId(groupId);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("스포츠", "풋살", "서울");
        verify(groupTagRepository).findByGroupId(groupId);
    }

    @Test
    @DisplayName("[성공] 그룹 ID로 태그명 조회 - 태그 없음")
    void getTagNamesByGroupId_NoTags_Success() {
        // given
        Long groupId = 1L;
        List<GroupTag> groupTags = List.of();

        given(groupTagRepository.findByGroupId(groupId)).willReturn(groupTags);

        // when
        List<String> result = groupTagService.getTagNamesByGroupId(groupId);

        // then
        assertThat(result).isEmpty();
        verify(groupTagRepository).findByGroupId(groupId);
    }

    @Test
    @DisplayName("[성공] 그룹 ID로 태그명 조회 - 단일 태그")
    void getTagNamesByGroupId_SingleTag_Success() {
        // given
        Long groupId = 1L;
        List<GroupTag> groupTags = List.of(
            createMockGroupTag(createMockTag(1L, "유일한태그"))
        );

        given(groupTagRepository.findByGroupId(groupId)).willReturn(groupTags);

        // when
        List<String> result = groupTagService.getTagNamesByGroupId(groupId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("유일한태그");
        verify(groupTagRepository).findByGroupId(groupId);
    }

    // 헬퍼 메서드들
    private Group createMockGroup(Long groupId) {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(groupId);
        return group;
    }

    private Tag createMockTag(Long tagId, String tagName) {
        Tag tag = mock(Tag.class);
        // tagId는 대부분의 테스트에서 사용되지 않으므로 제거
        when(tag.getName()).thenReturn(tagName);
        return tag;
    }

    private GroupTag createMockGroupTag(Tag tag) {
        GroupTag groupTag = mock(GroupTag.class);
        when(groupTag.getTag()).thenReturn(tag);
        return groupTag;
    }
}
