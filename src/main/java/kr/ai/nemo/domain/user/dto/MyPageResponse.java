package kr.ai.nemo.domain.user.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MyPageResponse(
    Long userId,
    String nickname,
    String email,
    String profileImageUrl,
    String createdAt
) {
    // LocalDateTime을 받는 생성자 (JPA 전용)
    public MyPageResponse(Long userId, String nickname, String email, String profileImageUrl, LocalDateTime createdAt) {
        this(userId, nickname, email, profileImageUrl,
             createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
