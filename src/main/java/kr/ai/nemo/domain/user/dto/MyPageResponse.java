package kr.ai.nemo.domain.user.dto;

import java.time.LocalDateTime;

public record MyPageResponse(
    String nickname,
    String email,
    String profileImageUrl,
    LocalDateTime createdAt
) {}
