package kr.ai.nemo.domain.user.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MyPageResponse(
    String nickname,
    String email,
    String profileImageUrl,
    String createdAt
) {
    // LocalDateTime을 받는 생성자 (JPA 전용)
    public MyPageResponse(String nickname, String email, String profileImageUrl, LocalDateTime createdAt) {
        this(nickname, email, profileImageUrl, 
             createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
