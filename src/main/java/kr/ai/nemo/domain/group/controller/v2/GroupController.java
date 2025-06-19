package kr.ai.nemo.domain.group.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.dto.response.GroupRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotSessionResponse;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.common.constants.CookieConstants;
import kr.ai.nemo.global.swagger.groupparticipant.SwaggerGroupParticipantsListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모임 API", description = "모임 관련 API 입니다.")
@RestController("groupControllerV2")
@RequestMapping("/api/v2/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupCommandService groupCommandService;
  private final GroupQueryService groupQueryService;
  private final AiGroupService aiGroupService;

  @Operation(summary = "모임 해체", description = "모임을 해체합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @TimeTrace
  @DeleteMapping("/{groupId}")
  public ResponseEntity<BaseApiResponse<Object>> deleteGroup(
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupCommandService.deleteGroup(groupId, userDetails.getUserId());
    aiGroupService.notifyGroupDeleted(groupId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "모임 대표 사진 수정", description = "모임을 해체합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @TimeTrace
  @PatchMapping("/{groupId}/image")
  public ResponseEntity<BaseApiResponse<Object>> updateGroupImage(
      @PathVariable Long groupId,
      @RequestBody UpdateGroupImageRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupCommandService.updateGroupImage(groupId, userDetails.getUserId(), request);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "텍스트 기반 모임 추천", description = "모임을 추천합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupParticipantsListResponse.class)))
  @TimeTrace
  @PostMapping("/recommendations/freeform")
  public ResponseEntity<BaseApiResponse<GroupRecommendResponse>> recommendGroupFreeform(
      @RequestBody GroupRecommendRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(
        groupCommandService.recommendGroupFreeform(request, userDetails.getUserId())));
  }

  @Operation(summary = "선택지 기반 모임 추천 - session 정보 가져오가", description = "기존에 대화 세션이 있었는지 확인합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
  @TimeTrace
  @GetMapping("/recommendations/session")
  public ResponseEntity<BaseApiResponse<GroupChatbotSessionResponse>> getChatbotSession(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = CookieConstants.CHATBOT_SESSION_ID) String sessionId
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(
        groupQueryService.getChatbotSession(userDetails.getUserId(), sessionId)));
  }

  @Operation(summary = "선택지 기반 모임 추천 - 새 session 등록", description = "새로운 세션을 시작합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
  @TimeTrace
  @PostMapping("/recommendations/session")
  public ResponseEntity<BaseApiResponse<Object>> newChatbotSession(
      HttpServletResponse response,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    String sessionId = groupCommandService.createNewChatbotSession(userDetails);
    Cookie sessionCookie = new Cookie(CookieConstants.CHATBOT_SESSION_ID, sessionId);
    sessionCookie.setHttpOnly(true);
    sessionCookie.setSecure(true);
    sessionCookie.setPath("/api/v2/groups/recommendations");
    sessionCookie.setMaxAge(CookieConstants.CHATBOT_SESSION_TTL);
    response.addCookie(sessionCookie);

    return ResponseEntity.ok(BaseApiResponse.noContent());
  }

  @Operation(summary = "선택지 기반 모임 추천 - 질문 생성/답장", description = "모임의 질문을 생성/답장 합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
  @TimeTrace
  @PostMapping("/recommendations/questions")
  public ResponseEntity<BaseApiResponse<GroupChatbotQuestionResponse>> recommendGroupQuestions(
      @RequestBody GroupChatbotQuestionRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = "sessionId") String sessionId
      ) {
    return ResponseEntity.ok(BaseApiResponse.success(
        groupCommandService.recommendGroupQuestion(request, userDetails.getUserId(), sessionId)));
  }

  @Operation(summary = "선택지 기반 모임 추천 - 모임 추천 요청", description = "모든 선택지에 응답 후 모임 추천을 요청합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.")
  @TimeTrace
  @GetMapping("/recommendations")
  public ResponseEntity<BaseApiResponse<GroupRecommendResponse>> recommendGroupRecommendation(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @CookieValue(name = "sessionId") String sessionId
  ) {
    GroupChatbotSessionResponse chatbotSession = groupQueryService.getChatbotSession(
        userDetails.getUserId(), sessionId);
    return ResponseEntity.ok(BaseApiResponse.success(
        groupQueryService.recommendGroup(chatbotSession, sessionId)));
  }
}
