package kr.ai.nemo.domain.group.controller.v2;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = kr.ai.nemo.domain.group.controller.v2.GroupController.class)
@MockMember
@Import({JwtProvider.class})
@ActiveProfiles("test")
@DisplayName("GroupControllerV2 테스트")
class GroupControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private GroupCommandService groupCommandService;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;


  @Test
  @DisplayName("[성공] 모임 해체 테스트")
  void deleteGroup_Success() throws Exception {
    // given
    Long groupId = 1L;

    // when
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId)
            .with(csrf()))
        .andExpect(status().isNoContent());

    // then
    verify(groupCommandService).deleteGroup(any(), any()); // Service 호출 검증
  }
}
