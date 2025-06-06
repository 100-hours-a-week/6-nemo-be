package kr.ai.nemo.domain.groupparticipants.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.user.domain.User;
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

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

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
    this.status = Status.WITHDRAWN;
  }

  public void markAsJoined() {
    this.joinedAt = LocalDateTime.now();
    this.status = Status.JOINED;
  }

  public void setStatus(Status status) {
    this.status = status;
    this.deletedAt = LocalDateTime.now();
  }
}
