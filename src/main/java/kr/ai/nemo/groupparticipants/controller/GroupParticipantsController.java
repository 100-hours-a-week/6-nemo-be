package kr.ai.nemo.groupparticipants.controller;

import java.util.List;
import kr.ai.nemo.global.common.ApiResponse;
import kr.ai.nemo.groupparticipants.domain.enums.Role;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.dto.GroupParticipantDto;
import kr.ai.nemo.groupparticipants.dto.GroupParticipantsListResponse;
import kr.ai.nemo.groupparticipants.dto.MyGroupDto;
import kr.ai.nemo.groupparticipants.dto.MyGroupListResponse;
import kr.ai.nemo.groupparticipants.service.GroupParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupParticipantsController {
  private final GroupParticipantsService groupParticipantsService;

  @PostMapping("/{groupId}/applications")
  public ResponseEntity<Object> applyToGroup(
      @PathVariable Long groupId,
      @AuthenticationPrincipal Long userId){
    groupParticipantsService.applyToGroup(groupId, userId, Role.MEMBER, Status.JOINED);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{groupId}/participants")
  public ResponseEntity<ApiResponse<GroupParticipantsListResponse>> getGroupParticipants(@PathVariable Long groupId) {
    List<GroupParticipantDto> list = groupParticipantsService.getAcceptedParticipants(groupId);
    return ResponseEntity.ok(ApiResponse.success(new GroupParticipantsListResponse(list)));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MyGroupListResponse>> getMyGroups(@AuthenticationPrincipal Long userId) {
    List<MyGroupDto> groupList = groupParticipantsService.getMyGroups(userId);
    return ResponseEntity.ok(ApiResponse.success(new MyGroupListResponse(groupList)));
  }
}
