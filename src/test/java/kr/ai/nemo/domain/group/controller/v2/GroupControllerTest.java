package kr.ai.nemo.domain.group.controller.v2;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import kr.ai.nemo.global.testUtil.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
  private GroupQueryService groupQueryService;

  @MockitoBean
  private AiGroupService aiGroupService;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @Autowired
  private ObjectMapper objectMapper;


  @Test
  @DisplayName("[성공] 모임 해체 테스트")
  void deleteGroup_Success() throws Exception {
    // given
    Long groupId = 1L;

    doNothing().when(aiGroupService).notifyGroupDeleted(groupId);

    // when
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId)
            .with(csrf()))
        .andExpect(status().isNoContent());

    // then
    verify(groupCommandService).deleteGroup(any(), any()); // Service 호출 검증
  }

  @Test
  @DisplayName("[성공] 모임 대표 사진 수정 테스트")
  void updateGroupImage_Success() throws Exception {
    // given
    Long groupId = 1L;
    Long userId = 1L;

    UpdateGroupImageRequest request = new UpdateGroupImageRequest(
        "newGroupImage"
    );

    // when
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", groupId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // then
    verify(groupCommandService).updateGroupImage(groupId, userId, request); // Service 호출 검증
  }
}
