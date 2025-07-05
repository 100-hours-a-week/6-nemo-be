package kr.ai.nemo.domain.groupparticipants.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.kafka.producer.KafkaNotifyGroupService;
import kr.ai.nemo.global.swagger.jwt.SwaggerJwtErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모임원 API (v2)", description = "모임원 관련 API 입니다.")
@RestController("groupParticipantsControllerV2")
@RequestMapping("/api/v2/groups")
@RequiredArgsConstructor
public class GroupParticipantsController {

  private final GroupParticipantsCommandService groupParticipantsCommandService;
  private final AiGroupService aiGroupService;
  private final KafkaNotifyGroupService kafkaNotifyGroupService;

  @Operation(summary = "모임원 추방", description = "모임장이 모임원을 추방합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @ApiResponse(responseCode = "403", description = "추방 권한이 없습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @SwaggerJwtErrorResponse
  @TimeTrace
  @DeleteMapping("/{groupId}/participants/{userId}")
  public ResponseEntity<BaseApiResponse<Object>> deleteGroupParticipants(
      @PathVariable Long groupId,
      @PathVariable Long userId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupParticipantsCommandService.kickOut(groupId, userId, userDetails);
    kafkaNotifyGroupService.notifyGroupLeft(userId, groupId);
    /*
    이전 WebClient 코드
    aiGroupService.notifyGroupLeft(userId, groupId);
    */

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "모임 탈퇴", description = "모임을 탈퇴합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @SwaggerJwtErrorResponse
  @TimeTrace
  @DeleteMapping("/{groupId}/participants/me")
  public ResponseEntity<BaseApiResponse<Object>> withdrawGroup(
      @PathVariable Long groupId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupParticipantsCommandService.withdrawGroup(groupId, userDetails.getUserId());
    kafkaNotifyGroupService.notifyGroupLeft(userDetails.getUserId(), groupId);
    /*
    이전 WebClient 코드
    aiGroupService.notifyGroupLeft(userId, groupId);
    */
    return ResponseEntity.noContent().build();
  }
}
