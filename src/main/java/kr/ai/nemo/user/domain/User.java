package kr.ai.nemo.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.ai.nemo.user.domain.enums.UserStatus;
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

  @Column(name = "nickname", nullable = false)
  private String nickname;

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
}
