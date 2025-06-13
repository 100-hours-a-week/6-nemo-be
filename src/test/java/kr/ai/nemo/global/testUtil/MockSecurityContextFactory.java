package kr.ai.nemo.global.testUtil;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class MockSecurityContextFactory implements WithSecurityContextFactory<MockMember> {

  @Override
  public SecurityContext createSecurityContext(MockMember mockMember) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    User mockUser = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(mockUser, "id", 1L);
    CustomUserDetails userDetails = new CustomUserDetails(mockUser);

    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities()
    );

    context.setAuthentication(auth);
    return context;
  }
}
