package kr.ai.nemo.group.participants.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "group_participants")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupParticipants {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Column(name = "role", nullable = false)
  private String role;

  @Setter
  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "applied_at", nullable = false)
  private LocalDateTime appliedAt;

  @Setter
  @Column(name = "joined_at")
  private LocalDateTime joinedAt;

  @Setter
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void markAsDeleted() {
    this.deletedAt = LocalDateTime.now();
    this.status = "WITHDRAWN";
  }

  public void markAsJoined() {
    this.joinedAt = LocalDateTime.now();
    this.status = "JOINED";
  }
}
