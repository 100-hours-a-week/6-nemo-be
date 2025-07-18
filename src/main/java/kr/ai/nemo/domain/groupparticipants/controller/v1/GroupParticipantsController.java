package kr.ai.nemo.domain.groupparticipants.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupListResponse;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsQueryService;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.global.kafka.producer.KafkaNotifyGroupService;
import kr.ai.nemo.global.swagger.groupparticipant.SwaggerGroupParticipantsListResponse;
import kr.ai.nemo.global.swagger.groupparticipant.SwaggerMyGroupListResponse;
import kr.ai.nemo.global.swagger.jwt.SwaggerJwtErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "모임원 API (v1)", description = "모임원 관련 API 입니다.")
@RestController("groupParticipantsControllerV1")
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupParticipantsController {
  private final GroupParticipantsCommandService groupParticipantsCommandService;
  private final GroupParticipantsQueryService groupParticipantsQueryService;
  private final AiGroupService aiGroupService;
  private final KafkaNotifyGroupService kafkaNotifyGroupService;
  private final GroupEventPublisher groupEventPublisher;

  @Operation(summary = "모임 신청", description = "사용자가 특정 모임에 가입 신청을 합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @ApiResponse(responseCode = "409", description = "이미 신청했거나 참여중인 사용자입니다. // 모임이 가득 찼습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @SwaggerJwtErrorResponse
  @TimeTrace
  @PostMapping("/{groupId}/applications")
  public ResponseEntity<Object> applyToGroup(
      @Parameter(description = "모임 ID", example = "123")
      @PathVariable Long groupId,
      @Parameter(hidden = true)
      @AuthenticationPrincipal CustomUserDetails userDetails){
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);
    groupEventPublisher.publishGroupJoined(userDetails.getUserId(), groupId);

    /*
    이전 kafka 코드 (interface 전)
    kafkaNotifyGroupService.notifyGroupJoined(userDetails.getUserId(), groupId);

    이전 WebClient 코드
    aiGroupService.notifyGroupJoined(userDetails.getUserId(), groupId);
     */

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "모임원 list 조회", description = "특정 모임에 가입중인 사용자 목록을 반환합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupParticipantsListResponse.class)))
  @TimeTrace
  @GetMapping("/{groupId}/participants")
  public ResponseEntity<BaseApiResponse<GroupParticipantsListResponse>> getGroupParticipants(
      @Parameter(description = "모임 ID", example = "123")
      @PathVariable Long groupId) {
    List<GroupParticipantsListResponse.GroupParticipantDto> list = groupParticipantsQueryService.getAcceptedParticipants(groupId);
    return ResponseEntity.ok(BaseApiResponse.success(new GroupParticipantsListResponse(list)));
  }

  @Operation(summary = "내가 참여 중인 모임 list 조회", description = "현재 로그인한 사용자가 가입중인 모임 목록을 반환합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerMyGroupListResponse.class)))
  @SwaggerJwtErrorResponse
  @TimeTrace
  @GetMapping("/me")
  public ResponseEntity<BaseApiResponse<MyGroupListResponse>> getMyGroups(
      @Parameter(hidden = true)
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<MyGroupDto> groupList = groupParticipantsQueryService.getMyGroups(userDetails.getUserId());
    return ResponseEntity.ok(BaseApiResponse.success(new MyGroupListResponse(groupList)));
  }
}
