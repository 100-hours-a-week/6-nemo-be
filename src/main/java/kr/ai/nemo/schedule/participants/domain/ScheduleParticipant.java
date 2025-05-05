package kr.ai.nemo.schedule.participants.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.schedule.participants.domain.enums.ScheduleParticipantStatus;
import lombok.*;

@Entity
@Table(name = "schedule_participants",
    indexes = {
        @Index(name = "idx_user_id_status_joined_at", columnList = "user_id, status, joined_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ScheduleParticipantStatus status;

  @Column(name = "joined_at")
  private LocalDateTime joinedAt;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void accept() {
    this.status = ScheduleParticipantStatus.ACCEPTED;
    this.joinedAt = LocalDateTime.now();
  }

  public void reject() {
    this.status = ScheduleParticipantStatus.REJECTED;
    this.joinedAt = null;
  }

  public void cancel() {
    this.status = ScheduleParticipantStatus.CANCELED;
    this.joinedAt = null;
  }
}
