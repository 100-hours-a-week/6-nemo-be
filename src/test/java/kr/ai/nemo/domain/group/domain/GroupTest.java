package kr.ai.nemo.domain.group.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Group 도메인 테스트")
class GroupTest {

    @Test
    @DisplayName("[성공] Group 생성")
    void createGroup_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        String name = "테스트 그룹";
        String summary = "그룹 요약";
        String description = "그룹 설명";
        String plan = "그룹 계획";
        String category = "운동";
        String location = "서울시 강남구";
        String imageUrl = "https://example.com/image.jpg";
        int maxUserCount = 10;

        // when
        Group group = Group.builder()
                .owner(owner)
                .name(name)
                .summary(summary)
                .description(description)
                .plan(plan)
                .category(category)
                .location(location)
                .completedScheduleTotal(0)
                .imageUrl(imageUrl)
                .currentUserCount(1)
                .maxUserCount(maxUserCount)
                .status(GroupStatus.ACTIVE)
                .build();

        // then
        assertThat(group.getOwner()).isEqualTo(owner);
        assertThat(group.getName()).isEqualTo(name);
        assertThat(group.getSummary()).isEqualTo(summary);
        assertThat(group.getDescription()).isEqualTo(description);
        assertThat(group.getPlan()).isEqualTo(plan);
        assertThat(group.getCategory()).isEqualTo(category);
        assertThat(group.getLocation()).isEqualTo(location);
        assertThat(group.getCompletedScheduleTotal()).isEqualTo(0);
        assertThat(group.getImageUrl()).isEqualTo(imageUrl);
        assertThat(group.getCurrentUserCount()).isEqualTo(1);
        assertThat(group.getMaxUserCount()).isEqualTo(maxUserCount);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.ACTIVE);
    }

    @Test
    @DisplayName("[성공] 현재 사용자 수 증가")
    void addCurrentUserCount_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .currentUserCount(5)
                .maxUserCount(10)
                .status(GroupStatus.ACTIVE)
                .build();

        // when
        group.addCurrentUserCount();

        // then
        assertThat(group.getCurrentUserCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("[성공] 현재 사용자 수 감소")
    void decreaseCurrentUserCount_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .currentUserCount(5)
                .maxUserCount(10)
                .status(GroupStatus.ACTIVE)
                .build();

        // when
        group.decreaseCurrentUserCount();

        // then
        assertThat(group.getCurrentUserCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("[성공] 그룹 삭제")
    void deleteGroup_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        // when
        group.deleteGroup();

        // then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.DISBANDED);
        assertThat(group.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 이미지 URL 설정")
    void setImageUrl_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .imageUrl("old-image-url")
                .status(GroupStatus.ACTIVE)
                .build();

        String newImageUrl = "new-image-url";

        // when
        group.setImageUrl(newImageUrl);

        // then
        assertThat(group.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("[성공] 업데이트 시간 설정")
    void setUpdatedAt_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        LocalDateTime newUpdatedAt = LocalDateTime.now();

        // when
        group.setUpdatedAt(newUpdatedAt);

        // then
        assertThat(group.getUpdatedAt()).isEqualTo(newUpdatedAt);
    }

    @Test
    @DisplayName("[성공] 현재 사용자 수 설정")
    void setCurrentUserCount_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .currentUserCount(5)
                .status(GroupStatus.ACTIVE)
                .build();

        int newCount = 8;

        // when
        group.setCurrentUserCount(newCount);

        // then
        assertThat(group.getCurrentUserCount()).isEqualTo(newCount);
    }

    @Test
    @DisplayName("[성공] 상태 설정")
    void setStatus_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        // when
        group.setStatus(GroupStatus.DISBANDED);

        // then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.DISBANDED);
    }

    @Test
    @DisplayName("[성공] 빌더 기본값 확인")
    void builderDefaults_Success() {
        // given
        User owner = UserFixture.createDefaultUser();

        // when
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        // then
        assertThat(group.getGroupParticipants()).isNotNull().isEmpty();
        assertThat(group.getGroupTags()).isNotNull().isEmpty();
    }
}
