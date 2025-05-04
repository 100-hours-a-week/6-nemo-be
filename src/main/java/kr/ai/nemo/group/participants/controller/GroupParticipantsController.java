package kr.ai.nemo.group.participants.controller;

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
  private GroupParticipantsService groupParticipantsService;

  @PostMapping("groups/{groupId}/applications")
  public void applyToGroup(@PathVariable Long groupId){
    Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    groupParticipantsService.applyToGroup(groupId, userId);
  }
}