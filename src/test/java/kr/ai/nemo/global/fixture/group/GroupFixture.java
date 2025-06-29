package kr.ai.nemo.global.fixture.group;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;

/**
 * 테스트용 Group 픽스처 클래스
 */
public class GroupFixture {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    public static Group createGroup(User owner, String name, String category, int maxUserCount) {
        return Group.builder()
                .owner(owner)
                .name(name)
                .summary("테스트 그룹 요약")
                .description("테스트 그룹 설명")
                .plan("테스트 그룹 계획")
                .category(category)
                .location("서울시 강남구")
                .completedScheduleTotal(0)
                .imageUrl("https://example.com/group-image.jpg")
                .currentUserCount(1)
                .maxUserCount(maxUserCount)
                .status(GroupStatus.ACTIVE)
                .build();
    }

    public static Group createDefaultGroup(User owner) {
        return createGroup(owner, "테스트 그룹", "운동", 10);
    }

    public static Group createFullGroup(User owner) {
        Group group = createGroup(owner, "가득찬 그룹", "운동", 5);
        group.setCurrentUserCount(5); // 정원 가득참
        return group;
    }

    public static Group createDisbandedGroup(User owner) {
        Group group = createGroup(owner, "해체된 그룹", "운동", 10);
        group.setStatus(GroupStatus.DISBANDED);
        return group;
    }

    public static Group createGroupWithId(Long id, User owner, String name) {
        Group group = createGroup(owner, name, "운동", 10);
        TestReflectionUtils.setField(group, "id", id);
        return group;
    }
}
