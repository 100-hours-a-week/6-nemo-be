package kr.ai.nemo.domain.group.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tag_id")
  private Tag tag;

  @Builder
  public GroupTag(Group group, Tag tag) {
    this.group = group;
    this.tag = tag;
  }

  public void setGroup(Group group) {
    if (this.group != null) {
      this.group.getGroupTags().remove(this);
    }
    this.group = group;
    if (group != null && !group.getGroupTags().contains(this)) {
      group.getGroupTags().add(this);
    }
  }

  public void setTag(Tag tag) {
    if (this.tag != null) {
      this.tag.getGroupTags().remove(this);
    }
    this.tag = tag;
    if (tag != null && !tag.getGroupTags().contains(this)) {
      tag.getGroupTags().add(this);
    }
  }
}
