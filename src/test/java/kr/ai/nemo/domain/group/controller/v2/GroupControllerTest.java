package kr.ai.nemo.domain.group.controller.v2;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.domain.auth.service.CustomUserDetailsService;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.group.service.ChatbotSseService;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import kr.ai.nemo.global.kafka.producer.KafkaNotifyGroupService;
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

  @MockitoBean
  private KafkaNotifyGroupService kafkaNotifyGroupService;

  @MockitoBean
  private GroupEventPublisher groupEventPublisher;

  @MockitoBean
  private ChatbotSseService chatbotSseService;

  @Autowired
  private ObjectMapper objectMapper;

  // ===== 모임 해체 테스트 =====

  @Test
  @DisplayName("[성공] 모임 해체 테스트")
  void deleteGroup_Success() throws Exception {
    // given
    Long groupId = 1L;
    doNothing().when(groupCommandService).deleteGroup(eq(groupId), anyLong());
    doNothing().when(aiGroupService).notifyGroupDeleted(groupId);
    doNothing().when(groupEventPublisher).publishGroupDeleted(groupId);

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId)
            .with(csrf()))
        .andExpect(status().isNoContent());

    verify(groupCommandService).deleteGroup(eq(groupId), anyLong());
  }

  @Test
  @DisplayName("[실패] 모임 해체 - CSRF 토큰 없음")
  void deleteGroup_NoCsrfToken_Forbidden() throws Exception {
    // given
    Long groupId = 1L;

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 해체 - 존재하지 않는 모임")
  void deleteGroup_NotFound() throws Exception {
    // given
    Long nonExistentGroupId = 999L;
    doThrow(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))
        .when(groupCommandService).deleteGroup(eq(nonExistentGroupId), anyLong());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}", nonExistentGroupId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 모임 해체 - 권한 없음 (소유자가 아닌 경우)")
  void deleteGroup_Forbidden() throws Exception {
    // given
    Long groupId = 1L;
    doThrow(new GroupException(GroupErrorCode.GROUP_DELETE_FORBIDDEN))
        .when(groupCommandService).deleteGroup(eq(groupId), anyLong());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId)
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 해체 - 이미 해체된 모임")
  void deleteGroup_AlreadyDisbanded() throws Exception {
    // given
    Long groupId = 1L;
    doThrow(new GroupException(GroupErrorCode.GROUP_DISBANDED))
        .when(groupCommandService).deleteGroup(eq(groupId), anyLong());

    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}", groupId)
            .with(csrf()))
        .andExpect(status().isConflict());
  }

  // ===== 모임 이미지 수정 테스트 =====

  @Test
  @DisplayName("[성공] 모임 대표 사진 수정 테스트")
  void updateGroupImage_Success() throws Exception {
    // given
    Long groupId = 1L;
    UpdateGroupImageRequest request = new UpdateGroupImageRequest(
        "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAAAAAAAD//2Q=="
    );

    doNothing().when(groupCommandService).updateGroupImage(eq(groupId), anyLong(), eq(request));

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", groupId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(groupCommandService).updateGroupImage(eq(groupId), anyLong(), eq(request));
  }

  @Test
  @DisplayName("[실패] 모임 대표 사진 수정 - 존재하지 않는 모임")
  void updateGroupImage_GroupNotFound() throws Exception {
    // given
    Long nonExistentGroupId = 999L;
    UpdateGroupImageRequest request = new UpdateGroupImageRequest("validImage");

    doThrow(new GroupException(GroupErrorCode.GROUP_NOT_FOUND))
        .when(groupCommandService).updateGroupImage(eq(nonExistentGroupId), anyLong(), eq(request));

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", nonExistentGroupId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("[실패] 모임 대표 사진 수정 - 권한 없음")
  void updateGroupImage_Forbidden() throws Exception {
    // given
    Long groupId = 1L;
    UpdateGroupImageRequest request = new UpdateGroupImageRequest("validImage");
    
    doThrow(new GroupException(GroupErrorCode.GROUP_UPDATE_FORBIDDEN))
        .when(groupCommandService).updateGroupImage(eq(groupId), anyLong(), eq(request));

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", groupId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 대표 사진 수정 - CSRF 토큰 없음")
  void updateGroupImage_NoCsrfToken_Forbidden() throws Exception {
    // given
    Long groupId = 1L;
    UpdateGroupImageRequest request = new UpdateGroupImageRequest("validImage");

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", groupId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("[실패] 모임 대표 사진 수정 - 잘못된 Content-Type")
  void updateGroupImage_InvalidContentType_UnsupportedMediaType() throws Exception {
    // given
    Long groupId = 1L;
    UpdateGroupImageRequest request = new UpdateGroupImageRequest("newImage");

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", groupId)
            .with(csrf())
            .contentType(MediaType.TEXT_PLAIN)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @DisplayName("[실패] 잘못된 파라미터 타입")
  void accessWithInvalidParameter_BadRequest() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/v2/groups/{groupId}", "invalid")
            .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("[실패] 모임 해체 - 잘못된 HTTP 메서드")
  void deleteGroup_WrongMethod_MethodNotAllowed() throws Exception {
    // given
    Long groupId = 1L;

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}", groupId)
            .with(csrf()))
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @DisplayName("[성공] 모임 이미지 수정 - PNG 형식")
  void updateGroupImage_PngFormat_Success() throws Exception {
    // given
    Long groupId = 1L;
    UpdateGroupImageRequest request = new UpdateGroupImageRequest(
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
    );

    doNothing().when(groupCommandService).updateGroupImage(eq(groupId), anyLong(), eq(request));

    // when & then
    mockMvc.perform(patch("/api/v2/groups/{groupId}/image", groupId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(groupCommandService).updateGroupImage(eq(groupId), anyLong(), eq(request));
  }
}
