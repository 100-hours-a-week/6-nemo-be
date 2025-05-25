package kr.ai.nemo.groupparticipants.controller;

import java.util.List;
import kr.ai.nemo.global.common.ApiResponse;
import kr.ai.nemo.groupparticipants.domain.enums.Role;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.groupparticipants.dto.response.MyGroupListResponse;
import kr.ai.nemo.groupparticipants.service.GroupParticipantsQueryService;
import kr.ai.nemo.groupparticipants.service.GroupParticipantsCommandService;
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
  private final GroupParticipantsCommandService groupParticipantsCommandService;
  private final GroupParticipantsQueryService groupParticipantsQueryService;

  @PostMapping("/{groupId}/applications")
  public ResponseEntity<Object> applyToGroup(
      @PathVariable Long groupId,
      @AuthenticationPrincipal Long userId){
    groupParticipantsCommandService.applyToGroup(groupId, userId, Role.MEMBER, Status.JOINED);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{groupId}/participants")
  public ResponseEntity<ApiResponse<GroupParticipantsListResponse>> getGroupParticipants(@PathVariable Long groupId) {
    List<GroupParticipantsListResponse.GroupParticipantDto> list = groupParticipantsQueryService.getAcceptedParticipants(groupId);
    return ResponseEntity.ok(ApiResponse.success(new GroupParticipantsListResponse(list)));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MyGroupListResponse>> getMyGroups(@AuthenticationPrincipal Long userId) {
    List<MyGroupDto> groupList = groupParticipantsQueryService.getMyGroups(userId);
    return ResponseEntity.ok(ApiResponse.success(new MyGroupListResponse(groupList)));
  }
}
