package kr.ai.nemo.domain.auth.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import kr.ai.nemo.domain.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails, Serializable {

  private final transient User user;

  // 직렬화할 필드들만 별도로 저장
  private final Long userId;
  private final String email;

  public CustomUserDetails(User user) {
    this.user = user;
    this.userId = user.getId();
    this.email = user.getEmail();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList();
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return null;
  }
}
