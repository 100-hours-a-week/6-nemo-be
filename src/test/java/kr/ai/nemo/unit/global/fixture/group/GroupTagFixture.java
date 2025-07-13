package kr.ai.nemo.unit.global.fixture.group;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;

/**
 * 테스트용 GroupTag 픽스처 클래스
 */
public class GroupTagFixture {

  public static GroupTag create(Group group, Tag tag) {
    return GroupTag.builder()
        .group(group)
        .tag(tag)
        .build();
  }
}
