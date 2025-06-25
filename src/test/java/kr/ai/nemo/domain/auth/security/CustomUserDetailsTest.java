package kr.ai.nemo.domain.auth.security;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("CustomUserDetails 생성 및 필드 접근 테스트")
    void createCustomUserDetails() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .status(UserStatus.ACTIVE)
                .build();

        // when
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // then
        assertThat(userDetails.getUser()).isEqualTo(user);
        assertThat(userDetails.getUserId()).isEqualTo(1L);
        assertThat(userDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isNull();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("UserDetails 인터페이스 메서드 테스트")
    void userDetailsInterfaceMethods() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .status(UserStatus.ACTIVE)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // when & then
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
}
