package kr.ai.nemo.auth.repository;

import java.util.Optional;
import kr.ai.nemo.auth.domain.UserToken;
import kr.ai.nemo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

  Optional<UserToken> findByUserAndProvider(User user, String provider);
}
