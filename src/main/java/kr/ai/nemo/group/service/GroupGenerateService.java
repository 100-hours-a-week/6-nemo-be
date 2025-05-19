package kr.ai.nemo.group.service;

import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
import kr.ai.nemo.group.dto.GroupGenerateRequest;
import kr.ai.nemo.group.dto.GroupGenerateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupGenerateService {

  private final AiGroupGenerateClient aiClient;

  public GroupGenerateResponse generate(GroupGenerateRequest request) {
    GroupAiGenerateRequest aiRequest = GroupAiGenerateRequest.from(request);
    GroupAiGenerateResponse aiResponse = aiClient.call(aiRequest);
    return GroupGenerateResponse.from(request, aiResponse);
  }
}
