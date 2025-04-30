package kr.ai.nemo.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class GroupAiGenerateResponse {

  private String name;

  private String summary;

  private String description;

  private List<String> tags;

  private String plan;
}
