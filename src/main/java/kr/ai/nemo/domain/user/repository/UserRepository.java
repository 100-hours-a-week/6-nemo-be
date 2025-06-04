package kr.ai.nemo.domain.user.repository;

import java.util.Optional;
import kr.ai.nemo.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByProviderAndProviderId(String provider, String providerId);

  Optional<User> findByEmail(String email);
}
