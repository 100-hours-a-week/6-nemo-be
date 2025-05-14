package kr.ai.nemo.auth.repository;

import java.util.Optional;
import kr.ai.nemo.auth.domain.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

  Optional<UserToken> findByUserIdAndProvider(Long userId, String provider);

  Optional<UserToken> findByRefreshTokenAndRevokedFalse(String refreshToken);
}
