package kr.ai.nemo.domain.user.repository;

import java.util.Optional;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByProviderAndProviderId(String provider, String providerId);

  @Query("""
    SELECT new kr.ai.nemo.domain.user.dto.MyPageResponse(u.nickname, u.email, u.profileImageUrl, u.createdAt)
    FROM User u
    WHERE u.id = :id
""")
  MyPageResponse findDtoById(Long id);
}
