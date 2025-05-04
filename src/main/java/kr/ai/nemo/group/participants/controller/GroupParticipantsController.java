package kr.ai.nemo.group.participants.controller;

import java.util.List;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.group.participants.dto.GroupParticipantDto;
import kr.ai.nemo.group.participants.dto.GroupParticipantsListResponse;
import kr.ai.nemo.group.participants.service.GroupParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<Object> applyToGroup(@PathVariable Long groupId){
    Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    groupParticipantsService.applyToGroup(groupId, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/groups/{groupId}/participants")
  public ResponseEntity<ApiResponse<GroupParticipantsListResponse>> getGroupParticipants(@PathVariable Long groupId) {
    List<GroupParticipantDto> list = groupParticipantsService.getAcceptedParticipants(groupId);
    return ResponseEntity.ok(ApiResponse.success(new GroupParticipantsListResponse(list)));
  }
}