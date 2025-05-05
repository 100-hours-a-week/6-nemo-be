package kr.ai.nemo.schedule.domain;

import jakarta.persistence.*;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.user.domain.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Column(nullable = false, length = 30)
  private String title;

  @Column(nullable = false, length = 255)
  private String description;

  @Column(length = 100)
  private String address;

  @Column(name = "current_user_count", nullable = false)
  private int currentUserCount = 1;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ScheduleStatus status = ScheduleStatus.RECRUITING;

  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public Schedule(Group group, User owner, String title, String description,
      String address, int currentUserCount, ScheduleStatus status, LocalDateTime startAt) {
    this.group = group;
    this.owner = owner;
    this.title = title;
    this.description = description;
    this.address = address;
    this.currentUserCount = currentUserCount;
    this.status = status;
    this.startAt = startAt;
  }

  public void increaseCurrentUserCount() {
    this.currentUserCount++;
  }

  public void cancel() {
    this.status = ScheduleStatus.CANCELED;
    this.deletedAt = LocalDateTime.now();
  }
}
