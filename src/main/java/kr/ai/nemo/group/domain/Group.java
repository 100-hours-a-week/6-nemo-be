package kr.ai.nemo.group.domain;

import jakarta.persistence.*;
import kr.ai.nemo.group.domain.enums.Category;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups_table")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;


  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "summary", nullable = false)
  private String summary;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "plan")
  private String plan;

  @Column(name = "category", nullable = false)
  @Enumerated(EnumType.STRING)
  private Category category;

  @Column(name = "location", nullable = false)
  private String location;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "completed_schedule_total", nullable = false)
  private int completedScheduleTotal;

  @Column(name = "current_user_count", nullable = false)
  private int currentUserCount;

  @Column(name = "max_user_count", nullable = false)
  private int maxUserCount;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private GroupStatus status;

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<GroupTag> groupTags = new ArrayList<>();

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public Group(User owner, String name, String summary, String description, String plan,
      Category category, String location, String imageUrl,
      int completedScheduleTotal, int currentUserCount, int maxUserCount, GroupStatus status) {
    this.owner = owner;
    this.name = name;
    this.summary = summary;
    this.description = description;
    this.plan = plan;
    this.category = category;
    this.location = location;
    this.imageUrl = imageUrl;
    this.completedScheduleTotal = completedScheduleTotal;
    this.currentUserCount = currentUserCount;
    this.maxUserCount = maxUserCount;
    this.status = status;
  }

  public void setCategory(String categoryDisplayName) {
    this.category = Category.fromDisplayName(categoryDisplayName);
  }

  public String getCategoryDisplayName() {
    return Category.toDisplayName(this.category);
  }

}