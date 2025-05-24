package kr.ai.nemo.group.controller;

import jakarta.validation.Valid;
import kr.ai.nemo.global.common.ApiResponse;
import kr.ai.nemo.group.dto.GroupCreateRequest;
import kr.ai.nemo.group.dto.GroupCreateResponse;
import kr.ai.nemo.group.dto.GroupDetailResponse;
import kr.ai.nemo.group.dto.GroupGenerateRequest;
import kr.ai.nemo.group.dto.GroupGenerateResponse;
import kr.ai.nemo.group.dto.GroupListResponse;
import kr.ai.nemo.group.dto.GroupSearchRequest;
import kr.ai.nemo.group.service.GroupCommandService;
import kr.ai.nemo.group.service.GroupGenerateService;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.schedule.dto.PageRequestDto;
import kr.ai.nemo.schedule.dto.ScheduleListResponse;
import kr.ai.nemo.schedule.service.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

  private final GroupGenerateService groupGenerateService;
  private final GroupCommandService groupCommandService;
  private final GroupQueryService groupQueryService;
  private final ScheduleQueryService scheduleQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<GroupListResponse>> getGroups(@Valid @ModelAttribute GroupSearchRequest request) {
    return ResponseEntity.ok(ApiResponse.success(groupQueryService.getGroups(request)));
  }

  @GetMapping("/{groupId}")
  public ResponseEntity<ApiResponse<GroupDetailResponse>> showGroupInfo(@PathVariable Long groupId) {
    return ResponseEntity.ok(ApiResponse.success(groupQueryService.detailGroup(groupId)));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<GroupListResponse>> searchGroups(@Valid @ModelAttribute GroupSearchRequest request) {
    return ResponseEntity.ok(ApiResponse.success(groupQueryService.getGroups(request)));
  }

  @GetMapping("/{groupId}/schedules")
  public ResponseEntity<ApiResponse<ScheduleListResponse>> getGroupSchedules(
      @PathVariable Long groupId,
      @Valid PageRequestDto pageRequestDto
  ) {
    return ResponseEntity.ok(ApiResponse.success(
        scheduleQueryService.getGroupSchedules(groupId, pageRequestDto.toPageRequest("startAt", "desc"))
    ));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<GroupCreateResponse>> createGroup(
      @Valid @RequestBody GroupCreateRequest request,
      @AuthenticationPrincipal Long userId ) {

    GroupCreateResponse createdGroup = groupCommandService.createGroup(request, userId);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(createdGroup.getId())
        .toUri();

    return ResponseEntity
        .created(location)
        .body(ApiResponse.created(createdGroup));
  }

  @PostMapping("/ai-generate")
  public ResponseEntity<ApiResponse<GroupGenerateResponse>> generateGroupInfo(
      @Valid @RequestBody GroupGenerateRequest request
  ) {
    return ResponseEntity.ok(ApiResponse.success(groupGenerateService.generate(request)));
  }
}
