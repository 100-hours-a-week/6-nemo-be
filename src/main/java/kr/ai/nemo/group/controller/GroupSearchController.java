package kr.ai.nemo.group.controller;

import kr.ai.nemo.group.dto.GroupListResponse;
import kr.ai.nemo.group.dto.GroupSearchRequest;
import kr.ai.nemo.group.service.GroupQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class GroupSearchController {

  private final GroupQueryService groupQueryService;

  @GetMapping
  public ResponseEntity<GroupListResponse> searchGroups(@ModelAttribute GroupSearchRequest request) {
    log.info("그룹 검색 요청: keyword={}, category={}, page={}, size={}, sort={}, direction={}",
        request.getKeyword(), request.getCategory(), request.getPage(),
        request.getSize(), request.getSort(), request.getDirection());
    
    GroupListResponse response = groupQueryService.getGroups(request);
    
    log.info("검색 완료: {}개 결과 반환", response.getGroups().size());
    
    return ResponseEntity.ok(response);
  }
}