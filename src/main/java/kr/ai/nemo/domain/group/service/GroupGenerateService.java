package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.domain.group.dto.request.GroupAiGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiGenerateResponse;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupGenerateResponse;
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
