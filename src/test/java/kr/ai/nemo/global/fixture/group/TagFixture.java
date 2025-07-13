package kr.ai.nemo.global.fixture.group;

import kr.ai.nemo.domain.group.domain.Tag;

public class TagFixture {

  public static Tag createDefaultTag() {
    return Tag.builder()
        .name("testTag")
        .build();
  }

  public static Tag createCustomTag(String name) {
    return Tag.builder()
        .name(name)
        .build();
  }
}
