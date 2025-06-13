package kr.ai.nemo.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import lombok.*;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "provider", nullable = false)
  private String provider;

  @Column(name = "provider_id", nullable = false, unique = true)
  private String providerId;

  @Column(name = "email", nullable = false)
  private String email;

  @Setter
  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Setter
  @Column(name = "profile_image_url", nullable = false)
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private UserStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder.Default
  @OneToMany(mappedBy = "owner")
  private List<Group> ownedGroups = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<GroupParticipants> groupParticipants = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
  private List<Schedule> schedule = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<ScheduleParticipant> scheduleParticipants = new ArrayList<>();
}
