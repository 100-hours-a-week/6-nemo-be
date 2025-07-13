package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import kr.ai.nemo.domain.group.dto.request.GroupAiGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiGenerateResponse;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.config.AiApiProperties;
import kr.ai.nemo.global.error.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiGroupService 테스트")
class AiGroupServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private AiApiProperties aiApiProperties;

    @Mock
    private GroupValidator groupValidator;

    @InjectMocks
    private AiGroupService aiGroupService;

    @BeforeEach
    void setUp() {
        lenient().when(aiApiProperties.getGroupGenerateUrl()).thenReturn("http://mock-server/generate");
        lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("[성공] 그룹 삭제 알림")
    void notifyGroupDeleted_Success() {
        // given
        Long groupId = 1L;
        lenient().when(aiApiProperties.getGroupDeleteUrl()).thenReturn("http://mock-server/delete");

        // when
        aiGroupService.notifyGroupDeleted(groupId);

        // then
        verify(restClient).post();
    }

    @Test
    @DisplayName("[실패] AI 서버 연결 실패 - RestClientException")
    void call_RestClientException_ThrowCustomException() {
        // given
        GroupAiGenerateRequest request = new GroupAiGenerateRequest(
            "네모를 찾아라", "네모를 찾기", "IT/개발", "1개월 이상", false
        );

        given(responseSpec.body(any(ParameterizedTypeReference.class)))
                .willThrow(new RestClientException("Connection failed"));

        // when & then
        assertThatThrownBy(() -> aiGroupService.call(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("AI 서버 연결에 실패했습니다");
    }

    @Test
    @DisplayName("[실패] AI 서버 연결 실패 - RuntimeException")
    void call_RuntimeException_ThrowCustomException() {
        // given
        GroupAiGenerateRequest request = new GroupAiGenerateRequest(
            "네모를 찾아라", "네모를 찾기", "IT/개발", "1개월 이상", false
        );

        given(responseSpec.body(any(ParameterizedTypeReference.class)))
                .willThrow(new RuntimeException("Unexpected error"));

        // when & then
        assertThatThrownBy(() -> aiGroupService.call(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("AI 서버 연결에 실패했습니다");
    }

    @Test
    @DisplayName("[실패] AI 서버 응답이 null")
    void call_NullResponse_ThrowCustomException() {
        // given
        GroupAiGenerateRequest request = new GroupAiGenerateRequest(
            "네모를 찾아라", "네모를 찾기", "IT/개발", "1개월 이상", false
        );

        given(responseSpec.body(any(ParameterizedTypeReference.class))).willReturn(null);

        // when & then
        assertThatThrownBy(() -> aiGroupService.call(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("AI 서버 연결에 실패했습니다");
    }

    @Test
    @DisplayName("[실패] AI 응답 데이터가 null")
    void call_NullData_ThrowCustomException() {
        // given
        GroupAiGenerateRequest request = new GroupAiGenerateRequest(
            "네모를 찾아라", "네모를 찾기", "IT/개발", "1개월 이상", false
        );

        BaseApiResponse<GroupAiGenerateResponse> responseWithNullData = new BaseApiResponse<>(200, "Success", null);
        given(responseSpec.body(any(ParameterizedTypeReference.class))).willReturn(responseWithNullData);

        // when & then
        assertThatThrownBy(() -> aiGroupService.call(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("AI 서버 연결에 실패했습니다");
    }
}
