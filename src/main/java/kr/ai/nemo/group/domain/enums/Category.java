package kr.ai.nemo.group.domain.enums;

import java.util.Arrays;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Category {
  PET("반려동물"),
  SPORTS("스포츠"),
  READING_DISCUSSION("독서/토론"),
  CULTURE_ART("문화/예술"),
  LANGUAGE_FOREIGN("외국/언어"),
  GAMES_ENTERTAINMENT("게임/오락"),
  MUSIC_INSTRUMENT("음악/악기"),
  IT_DEVELOPMENT("IT/개발"),
  CAREER_EMPLOYMENT("취업/커리어"),
  ECONOMY_FINANCE("경제/금융"),
  SOCIAL_NETWORKING("친목/사교"),
  ETC("기타");

  private final String displayName;

  public static Category fromDisplayName(String displayName) {
    return Arrays.stream(values())
        .filter(category -> category.displayName.equals(displayName))
        .findFirst()
        .orElseThrow(() -> new CustomException(ResponseCode.INVALID_ENUM));
  }

  public static String toDisplayName(Category category) {
    return category.displayName;
  }
}
