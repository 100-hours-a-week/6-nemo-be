package kr.ai.nemo.global.fixture.group;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.user.domain.User;

/**
 * 테스트용 Group 픽스처 클래스
 */
public class GroupFixture {

  public static Group createGroup(
      User owner, String name, String summary, String description, String plan,
      String category, String location, String imageUrl,
      int maxUserCount) {
    return Group.builder()
        .owner(owner)
        .name(name)
        .summary(summary)
        .description(description)
        .plan(plan)
        .category(category)
        .location(location)
        .completedScheduleTotal(0)
        .imageUrl(imageUrl)
        .currentUserCount(0)
        .maxUserCount(maxUserCount)
        .status(GroupStatus.ACTIVE)
        .build();
  }

  public static Group createDefaultGroup(User owner) {
    return createGroup(owner, "테스트 모임", "테스트 모임입니다.", "테스트입니다.", "테스트할 예정입니다.",
        "IT/개발", "서울특별시", "img.jpg", 10);
  }
}
