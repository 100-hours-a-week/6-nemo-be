package kr.ai.nemo.global.testUtil;

import java.util.List;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;

/**
 * 테스트 데이터 빌더 유틸리티 클래스
 * 일관된 테스트 데이터 생성을 위한 헬퍼 메서드들을 제공합니다.
 */
public class TestDataBuilder {

    // ===== Group 관련 Request 빌더 =====

    /**
     * 유효한 GroupCreateRequest 생성
     */
    public static GroupCreateRequest validGroupCreateRequest() {
        return new GroupCreateRequest(
            "테스트 모임",
            "테스트 모임 요약",
            "테스트 모임에 대한 상세 설명입니다.",
            "IT/개발",
            "서울 강남구",
            10,
            "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAAAAAAAD//2Q==",
            List.of("테스트", "자바", "스프링"),
            "모임 활동 계획입니다."
        );
    }

    /**
     * 이름이 빈 GroupCreateRequest 생성
     */
    public static GroupCreateRequest emptyNameGroupCreateRequest() {
        return new GroupCreateRequest(
            "",
            "테스트 모임 요약",
            "테스트 모임에 대한 상세 설명입니다.",
            "IT/개발",
            "서울 강남구",
            10,
            "test.jpg",
            List.of("테스트"),
            "모임 활동 계획입니다."
        );
    }

    /**
     * 유효한 Base64 JPEG 이미지 데이터
     */
    public static String validJpegBase64() {
        return "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAAAAAAAD//2Q==";
    }

    /**
     * 유효한 태그 리스트
     */
    public static List<String> validTags() {
        return List.of("자바", "스프링", "백엔드", "개발");
    }

    /**
     * 유효한 카테고리 리스트
     */
    public static List<String> validCategories() {
        return List.of("IT/개발", "스포츠", "취미", "문화/예술", "어학", "자기계발");
    }
}
