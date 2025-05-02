package kr.ai.nemo.group.controller;

import jakarta.validation.Valid;
import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
import kr.ai.nemo.group.dto.GroupCreateRequest;
import kr.ai.nemo.group.dto.GroupCreateResponse;
import kr.ai.nemo.group.service.AiGroupGenerateClient;
import kr.ai.nemo.group.service.GroupCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

  private final AiGroupGenerateClient aiGroupGenerateClient;
  private final GroupCommandService groupCommandService;

  @PostMapping("/ai-generate")
  public ResponseEntity<GroupAiGenerateResponse> generateGroupInfo(@Valid @RequestBody GroupAiGenerateRequest request) {
    GroupAiGenerateResponse aiResponse = aiGroupGenerateClient.call(request);
    return ResponseEntity.ok(aiResponse);
  }

  @PostMapping
  public ResponseEntity<GroupCreateResponse> createGroup(@Valid @RequestBody GroupCreateRequest request) {
    GroupCreateResponse createdGroup = groupCommandService.createGroup(request);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(createdGroup.getGroupId())
        .toUri();

    return ResponseEntity.created(location).body(createdGroup);
  }
}