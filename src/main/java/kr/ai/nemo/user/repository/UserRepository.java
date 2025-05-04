package kr.ai.nemo.user.repository;

import java.util.Optional;
import kr.ai.nemo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
