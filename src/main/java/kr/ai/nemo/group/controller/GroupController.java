package kr.ai.nemo.group.controller;

import jakarta.validation.Valid;
import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
import kr.ai.nemo.group.service.AiGroupGenerateClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

  private final AiGroupGenerateClient aiGroupGenerateClient;

  @PostMapping("/ai-generate")
  public ResponseEntity<GroupAiGenerateResponse> generateGroupInfo(@Valid @RequestBody GroupAiGenerateRequest request) {

    GroupAiGenerateResponse aiResponse = aiGroupGenerateClient.call(request);

    return ResponseEntity.ok(aiResponse);
  }
}
