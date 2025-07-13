package kr.ai.nemo.unit.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import kr.ai.nemo.infra.ImageService;
import kr.ai.nemo.unit.global.error.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService 테스트")
class ImageServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", "test-bucket");
    }

    // ===== 카카오 프로필 이미지 업로드 테스트 =====

    @Test
    @DisplayName("[성공] null 이미지 URL로 카카오 프로필 이미지 업로드")
    void uploadKakaoProfileImage_NullImageUrl_ReturnNull() {
        // when
        String result = imageService.uploadKakaoProfileImage(null, 1L);

        // then
        assertThat(result).isNull();
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[성공] 빈 이미지 URL로 카카오 프로필 이미지 업로드")
    void uploadKakaoProfileImage_EmptyImageUrl_ReturnNull() {
        // when
        String result = imageService.uploadKakaoProfileImage("   ", 1L);

        // then
        assertThat(result).isNull();
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[실패] 잘못된 Base64 형식으로 카카오 프로필 이미지 업로드")
    void uploadKakaoProfileImage_InvalidBase64_ThrowException() {
        // given
        Long userId = 1L;
        String invalidBase64Image = "data:image/jpeg;base64,invalid@#$%";

        // when & then
        assertThatThrownBy(() -> imageService.uploadKakaoProfileImage(invalidBase64Image, userId))
            .isInstanceOf(CustomException.class);
    }

    // ===== 그룹 이미지 업로드 테스트 =====

    @Test
    @DisplayName("[성공] null 그룹 이미지 업로드")
    void uploadGroupImage_NullImageUrl_ReturnNull() {
        // when
        String result = imageService.uploadGroupImage(null);

        // then
        assertThat(result).isNull();
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[성공] 빈 그룹 이미지 업로드")
    void uploadGroupImage_EmptyImageUrl_ReturnNull() {
        // when
        String result = imageService.uploadGroupImage("   ");

        // then
        assertThat(result).isNull();
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[성공] Base64 그룹 이미지 업로드")
    void uploadGroupImage_Base64Image_Success() throws MalformedURLException {
        // given
        String base64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

        URL mockUrl = URI.create("https://test-bucket.s3.amazonaws.com/groups/profile_123.png").toURL();
        given(amazonS3.getUrl(eq("test-bucket"), anyString())).willReturn(mockUrl);

        // when
        String result = imageService.uploadGroupImage(base64Image);

        // then
        assertThat(result).isEqualTo(mockUrl.toString());
        verify(amazonS3).putObject(eq("test-bucket"), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(amazonS3).getUrl(eq("test-bucket"), anyString());
    }

    @Test
    @DisplayName("[실패] 잘못된 Base64 형식으로 그룹 이미지 업로드")
    void uploadGroupImage_InvalidBase64_ThrowException() {
        // given
        String invalidBase64Image = "data:image/jpeg;base64,invalid@#$";

        // when & then
        assertThatThrownBy(() -> imageService.uploadGroupImage(invalidBase64Image))
            .isInstanceOf(CustomException.class);
    }

    // ===== 이미지 업데이트 테스트 =====

    @Test
    @DisplayName("[성공] 이미지 업데이트 - 새 이미지가 null인 경우")
    void updateImage_NewImageNull_ReturnNull() {
        // given
        String oldImageUrl = "https://external.com/image.jpg";
        String newImageUrl = null;

        // when
        String result = imageService.updateImage(oldImageUrl, newImageUrl);

        // then
        assertThat(result).isNull(); // uploadGroupImage(null)이 null을 반환
        verify(amazonS3, never()).deleteObject(anyString(), anyString());
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    // ===== 이미지 삭제 테스트 =====

    @Test
    @DisplayName("[성공] S3가 아닌 이미지 삭제 시도")
    void deleteImage_NonS3Image_DoNothing() {
        // given
        String externalImageUrl = "https://external.com/image.jpg";

        // when
        imageService.deleteImage(externalImageUrl);

        // then
        verify(amazonS3, never()).deleteObject(anyString(), anyString());
    }

    @Test
    @DisplayName("[성공] S3 이미지 삭제")
    void deleteImage_S3Image_DeleteFromS3() {
        // given
        String s3ImageUrl = "https://test-bucket.s3.amazonaws.com/groups/profile_123.jpg";

        // when
        imageService.deleteImage(s3ImageUrl);

        // then
        verify(amazonS3).deleteObject(eq("test-bucket"), eq("groups/profile_123.jpg"));
    }

    @Test
    @DisplayName("[성공] null 이미지 삭제 시도")
    void deleteImage_NullImage_DoNothing() {
        // when
        imageService.deleteImage(null);

        // then
        verify(amazonS3, never()).deleteObject(anyString(), anyString());
    }

    @Test
    @DisplayName("[성공] 빈 이미지 삭제 시도")
    void deleteImage_BlankImage_DoNothing() {
        // when
        imageService.deleteImage("   ");

        // then
        verify(amazonS3, never()).deleteObject(anyString(), anyString());
    }

    // ===== 추가 엣지 케이스 테스트 =====

    @Test
    @DisplayName("[성공] 다양한 이미지 포맷 Base64 업로드 - PNG")
    void uploadGroupImage_PngBase64_Success() throws MalformedURLException {
        // given
        String pngBase64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

        URL mockUrl = URI.create("https://test-bucket.s3.amazonaws.com/groups/profile_123.png").toURL();
        given(amazonS3.getUrl(eq("test-bucket"), anyString())).willReturn(mockUrl);

        // when
        String result = imageService.uploadGroupImage(pngBase64Image);

        // then
        assertThat(result).isEqualTo(mockUrl.toString());
        verify(amazonS3).putObject(eq("test-bucket"), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[성공] URL 기반 이미지 업로드")
    void uploadKakaoProfileImage_UrlImage_Success() throws Exception {
        // given
        Long userId = 1L;
        String pngBase64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

        URL mockUrl = URI.create("https://test-bucket.s3.amazonaws.com/users/1/profile_123.jpg").toURL();
        given(amazonS3.getUrl(eq("test-bucket"), anyString())).willReturn(mockUrl);

        // when
        String result = imageService.uploadKakaoProfileImage(pngBase64Image, userId);

        // then
        assertThat(result).isEqualTo(mockUrl.toString());
        verify(amazonS3).putObject(eq("test-bucket"), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[성공] S3 키 추출 테스트")
    void deleteImage_ExtractS3Key_Success() {
        // given
        String s3ImageUrl = "https://test-bucket.s3.amazonaws.com/users/123/profile_456.jpg";

        // when
        imageService.deleteImage(s3ImageUrl);

        // then
        verify(amazonS3).deleteObject(eq("test-bucket"), eq("users/123/profile_456.jpg"));
    }

    @Test
    @DisplayName("[성공] 빈 문자열이 아닌 공백 문자열 처리")
    void uploadImage_WhitespaceOnly_ReturnNull() {
        // given
        String whitespaceImage = "\t\n\r   ";

        // when
        String result = imageService.uploadGroupImage(whitespaceImage);

        // then
        assertThat(result).isNull();
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[실패] Base64 데이터 누락")
    void uploadImage_Base64WithoutData_ThrowException() {
        // given
        String invalidBase64 = "data:image/jpeg;base64,";

        // when & then
        assertThatThrownBy(() -> imageService.uploadGroupImage(invalidBase64))
            .isInstanceOf(CustomException.class);
    }
}
