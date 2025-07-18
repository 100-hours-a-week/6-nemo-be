package kr.ai.nemo.domain.user.repository;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import kr.ai.nemo.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByProviderAndProviderId(String provider, String providerId);

  @Query("""
    SELECT u
    FROM User u
    WHERE u.id = :id
""")
  Optional<User> findUserById(Long id);

  boolean existsByNickname(@NotNull @Min(2) @Max(20) String nickname);
}
