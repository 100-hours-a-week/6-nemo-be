package kr.ai.nemo.domain.group.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Tag 이름은 필수입니다.")
  @Column(nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy = "tag")
  private final List<GroupTag> groupTags = new ArrayList<>();

  @Builder
  public Tag(String name) {
    this.name = name;
  }
}
