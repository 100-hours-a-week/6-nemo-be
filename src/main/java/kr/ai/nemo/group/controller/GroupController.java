package kr.ai.nemo.group.controller;

import jakarta.validation.Valid;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
import kr.ai.nemo.group.dto.GroupCreateRequest;
import kr.ai.nemo.group.dto.GroupCreateResponse;
import kr.ai.nemo.group.dto.GroupDetailResponse;
import kr.ai.nemo.group.dto.GroupListResponse;
import kr.ai.nemo.group.dto.GroupSearchRequest;
import kr.ai.nemo.group.service.AiGroupGenerateClient;
import kr.ai.nemo.group.service.GroupCommandService;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.schedule.dto.PageRequestDto;
import kr.ai.nemo.schedule.dto.ScheduleListResponse;
import kr.ai.nemo.schedule.service.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

  private final AiGroupGenerateClient aiGroupGenerateClient;
  private final GroupCommandService groupCommandService;
  private final GroupQueryService groupQueryService;
  private final ScheduleQueryService scheduleQueryService;

  @PostMapping("/ai-generate")
  public ResponseEntity<ApiResponse<GroupAiGenerateResponse>> generateGroupInfo(@Valid @RequestBody GroupAiGenerateRequest request) {
    GroupAiGenerateResponse aiResponse = aiGroupGenerateClient.call(request);
    return ResponseEntity.ok(ApiResponse.success(aiResponse));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<GroupCreateResponse>> createGroup(@Valid @RequestBody GroupCreateRequest request) {
    Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    GroupCreateResponse createdGroup = groupCommandService.createGroup(request, userId);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(createdGroup.getGroupId())
        .toUri();

    return ResponseEntity
        .created(location)
        .body(ApiResponse.created(createdGroup));
  }

  @GetMapping("/{groupId}")
  public ResponseEntity<ApiResponse<GroupDetailResponse>> showGroupInfo(@PathVariable Long groupId) {
    GroupDetailResponse response = groupQueryService.detailGroup(groupId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<GroupListResponse>> getGroups(@Valid @ModelAttribute GroupSearchRequest request) {
    return ResponseEntity.ok(ApiResponse.success(groupQueryService.getGroups(request)));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<GroupListResponse>> searchGroups(@ModelAttribute GroupSearchRequest request) {
    return ResponseEntity.ok(ApiResponse.success(groupQueryService.getGroups(request)));
  }

  @GetMapping("/{groupId}/schedules")
  public ResponseEntity<ApiResponse<ScheduleListResponse>> getGroupSchedules(
      @PathVariable Long groupId,
      @Valid PageRequestDto pageRequestDto
  ) {
    return ResponseEntity.ok(ApiResponse.success(
        scheduleQueryService.getGroupSchedules(groupId, pageRequestDto.toPageRequest())
    ));
  }
}