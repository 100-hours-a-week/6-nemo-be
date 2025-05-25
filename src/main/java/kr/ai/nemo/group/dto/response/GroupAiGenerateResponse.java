package kr.ai.nemo.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(Include.NON_EMPTY)
public record GroupAiGenerateResponse(
    String name,
    String summary,
    String description,
    List<String> tags,
    String plan
) {}
