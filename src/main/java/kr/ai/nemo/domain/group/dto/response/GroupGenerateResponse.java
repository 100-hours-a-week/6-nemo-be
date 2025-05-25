package kr.ai.nemo.domain.group.dto.response;

import java.util.List;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

public record GroupGenerateResponse(

    @Schema(description = "모임 이름", example = "백엔드 스터디")
    String name,

    @Schema(description = "모임 카테고리", example = "IT/개발")
    String category,

    @Schema(description = "모임 요약", example = "Spring 학습에 집중하는 모임입니다.")
    String summary,

    @Schema(description = "모임 설명", example = "Spring Boot, JPA 등을 함께 공부하는 스터디입니다.")
    String description,

    @Schema(description = "추천 학습 계획", example = "1주차: 스프링 입문, 2주차: JPA 기초, ...")
    String plan,

    @Schema(description = "모임 위치", example = "서울특별시 강남구")
    String location,

    @Schema(description = "최대 참여 인원 수", example = "10")
    int maxUserCount,

    @Schema(description = "태그 목록", example = "[\"Spring\", \"JPA\", \"Docker\"]")
    List<String> tags

) {
  public static GroupGenerateResponse from(GroupGenerateRequest request, GroupAiGenerateResponse ai) {
    return new GroupGenerateResponse(
        request.name(),
        request.category(),
        ai.summary(),
        ai.description(),
        ai.plan(),
        request.location(),
        request.maxUserCount(),
        ai.tags()
    );
  }
}
