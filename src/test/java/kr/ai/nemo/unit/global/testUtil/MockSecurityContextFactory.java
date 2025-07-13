package kr.ai.nemo.unit.global.testUtil;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class MockSecurityContextFactory implements WithSecurityContextFactory<MockMember> {

  @Override
  public SecurityContext createSecurityContext(MockMember mockMember) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    // 기본 사용자 생성 (ID는 실제 테스트에서 관리)
    User mockUser = UserFixture.createDefaultUser();

    // 통합 테스트의 경우 실제 저장된 사용자 ID를 사용
    // 단위 테스트의 경우 고정 ID 사용
    TestReflectionUtils.setField(mockUser, "id", 1L);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);

    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities()
    );

    context.setAuthentication(auth);
    return context;
  }
}
