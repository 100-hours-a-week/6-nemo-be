package kr.ai.nemo.domain.group.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.swagger.groupparticipant.SwaggerGroupParticipantsListResponse;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.global.common.BaseApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @Operation(summary = "모임 해체", description = "모임을 해체합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @TimeTrace
  @DeleteMapping("/{groupId}")
  public ResponseEntity<BaseApiResponse<Object>> deleteGroup(
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupCommandService.deleteGroup(groupId, userDetails.getUserId());
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
  public ResponseEntity<BaseApiResponse<GroupDto>> recommendGroupFreeform(
      @RequestBody GroupRecommendRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(
        groupCommandService.recommendGroupFreeform(request, userDetails.getUserId())));
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
}
