package kr.ai.nemo.group.participants.controller;

import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.group.participants.service.GroupParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GroupParticipantsController {
  private final GroupParticipantsService groupParticipantsService;

  @PostMapping("/groups/{groupId}/applications")
  public ApiResponse<Void> applyToGroup(@PathVariable Long groupId){
    Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    groupParticipantsService.applyToGroup(groupId, userId);
    return ApiResponse.noContent();
  }
}