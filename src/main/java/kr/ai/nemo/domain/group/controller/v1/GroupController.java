package kr.ai.nemo.domain.group.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ai.nemo.domain.group.messaging.GroupEventPublisher;
import kr.ai.nemo.unit.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.service.AiGroupService;
import kr.ai.nemo.unit.global.common.BaseApiResponse;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupGenerateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.domain.group.service.GroupCommandService;
import kr.ai.nemo.domain.group.service.GroupQueryService;
import kr.ai.nemo.unit.global.dto.PageRequestDto;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleListResponse;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import kr.ai.nemo.unit.global.kafka.producer.KafkaNotifyGroupService;
import kr.ai.nemo.unit.global.swagger.group.SwaggerGroupCreateResponse;
import kr.ai.nemo.unit.global.swagger.group.SwaggerGroupDetailResponse;
import kr.ai.nemo.unit.global.swagger.group.SwaggerGroupGenerateResponse;
import kr.ai.nemo.unit.global.swagger.group.SwaggerScheduleListResponse;
import kr.ai.nemo.unit.global.swagger.jwt.SwaggerJwtErrorResponse;
import kr.ai.nemo.unit.global.swagger.group.SwaggerGroupListResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "모임 API", description = "모임 관련 API 입니다.")
@RestController("groupControllerV1")
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupCommandService groupCommandService;
  private final GroupQueryService groupQueryService;
  private final ScheduleQueryService scheduleQueryService;
  private final AiGroupService aiGroupService;
  private final KafkaNotifyGroupService kafkaNotifyGroupService;
  private final GroupEventPublisher groupEventPublisher;

  @Operation(summary = "모임 리스트 조회", description = "카테고리별 모임의 리스트를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 조회되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupListResponse.class)))
  @TimeTrace
  @GetMapping
  public ResponseEntity<BaseApiResponse<GroupListResponse>> getAllGroupList(@Valid @ModelAttribute GroupSearchRequest request, @ParameterObject @Valid PageRequestDto pageRequestDto) {
    PageRequest pageRequest = pageRequestDto.toPageRequest("createdAt", "desc");
    return ResponseEntity.ok(BaseApiResponse.success(groupQueryService.getGroups(request, pageRequest)));
  }

  @Operation(summary = "모임 상세 조회", description = "요청한 모임의 상세 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 조회되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupDetailResponse.class)))
  @TimeTrace
  @GetMapping("/{groupId}")
  public ResponseEntity<BaseApiResponse<GroupDetailResponse>> getGroupDetail(
      @PathVariable Long groupId,
      @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails customUserDetails) {
    return ResponseEntity.ok(BaseApiResponse.success(groupQueryService.detailGroup(groupId, customUserDetails)));
  }

  @Operation(summary = "검색한 모임 리스트 조회", description = "검색한 키워드가 포함된 모임의 리스트를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 조회되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupListResponse.class)))
  @TimeTrace
  @GetMapping("/search")
  public ResponseEntity<BaseApiResponse<GroupListResponse>> searchGroupList(@Valid @ModelAttribute GroupSearchRequest request, @ParameterObject @Valid PageRequestDto pageRequestDto) {
    PageRequest pageRequest = pageRequestDto.toPageRequest("", "desc");
    return ResponseEntity.ok(BaseApiResponse.success(groupQueryService.getGroups(request, pageRequest)));
  }

  @Operation(summary = "모임의 일정 리스트 조회", description = "특정 모임의 일정 리스트를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "성공적으로 조회되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerScheduleListResponse.class)))
  @TimeTrace
  @GetMapping("/{groupId}/schedules")
  public ResponseEntity<BaseApiResponse<ScheduleListResponse>> getGroupScheduleList(
      @PathVariable Long groupId,
      @Valid PageRequestDto pageRequestDto
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(
        scheduleQueryService.getGroupSchedules(groupId, pageRequestDto.toPageRequest("startAt", "desc"))
    ));
  }

  @Operation(summary = "모임 생성", description = "모임을 생성합니다.")
  @ApiResponse(responseCode = "201", description = "리소스가 성공적으로 생성되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupCreateResponse.class)))
  @SwaggerJwtErrorResponse
  @TimeTrace
  @PostMapping
  public ResponseEntity<BaseApiResponse<GroupCreateResponse>> createGroup(
      @Valid @RequestBody GroupCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    GroupCreateResponse createdGroup = groupCommandService.createGroup(request, userDetails);
    groupEventPublisher.publishGroupCreated(createdGroup);


    /*
    이전 kafka 코드 (interface 전)
    kafkaNotifyGroupService.notifyGroupCreated(createdGroup);

    이전 WebClient 코드
    aiGroupService.notifyGroupCreated(createdGroup);
     */

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(createdGroup.groupId())
        .toUri();

    return ResponseEntity
        .created(location)
        .body(BaseApiResponse.created(createdGroup));
  }

  @Operation(summary = "AI에 모임 정보 생성 요청", description = "사용자의 입력을 토대로 AI가 모임 정보를 생성해줍니다.")
  @ApiResponse(responseCode = "201", description = "리소스가 성공적으로 생성되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerGroupGenerateResponse.class)))
  @SwaggerJwtErrorResponse
  @TimeTrace
  @PostMapping("/ai-generate")
  public ResponseEntity<BaseApiResponse<GroupGenerateResponse>> generateGroup(
      @Valid @RequestBody GroupGenerateRequest request
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(groupCommandService.generate(request)));
  }
}
