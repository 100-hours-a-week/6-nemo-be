package kr.ai.nemo.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Uploader 테스트")
class S3UploaderTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3Uploader s3Uploader;

    @Test
    void setUp() {
        ReflectionTestUtils.setField(s3Uploader, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("[성공] 파일 업로드")
    void upload_Success() throws Exception {
        setUp();
        // given
        String dirName = "test";
        String originalFilename = "test.jpg";
        String contentType = "image/jpeg";
        long fileSize = 1024L;
        byte[] fileContent = "test content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/test/uuid_test.jpg";

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);
        given(multipartFile.getContentType()).willReturn(contentType);
        given(multipartFile.getSize()).willReturn(fileSize);
        given(multipartFile.getInputStream()).willReturn(inputStream);
        given(amazonS3.getUrl(eq("test-bucket"), anyString()))
                .willReturn(new URL(expectedUrl));

        // when
        String result = s3Uploader.upload(multipartFile, dirName);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(multipartFile).getOriginalFilename();
        verify(multipartFile).getContentType();
        verify(multipartFile).getSize();
        verify(multipartFile).getInputStream();
        verify(amazonS3).putObject(eq("test-bucket"), anyString(), eq(inputStream), any(ObjectMetadata.class));
        verify(amazonS3).getUrl(eq("test-bucket"), anyString());
    }

    @Test
    @DisplayName("[실패] 파일 업로드 - IOException")
    void upload_IOException_ThrowRuntimeException() throws Exception {
        setUp();
        // given
        String dirName = "test";
        String originalFilename = "test.jpg";
        String contentType = "image/jpeg";
        long fileSize = 1024L;

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);
        given(multipartFile.getContentType()).willReturn(contentType);
        given(multipartFile.getSize()).willReturn(fileSize);
        given(multipartFile.getInputStream()).willThrow(new IOException("파일 읽기 실패"));

        // when & then
        assertThatThrownBy(() -> s3Uploader.upload(multipartFile, dirName))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 파일 업로드 실패");
    }

    @Test
    @DisplayName("[성공] 파일명에 UUID가 포함됨")
    void upload_ContainsUUID_Success() throws Exception {
        setUp();
        // given
        String dirName = "images";
        String originalFilename = "profile.png";
        String contentType = "image/png";
        long fileSize = 2048L;
        byte[] fileContent = "image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/images/uuid_profile.png";

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);
        given(multipartFile.getContentType()).willReturn(contentType);
        given(multipartFile.getSize()).willReturn(fileSize);
        given(multipartFile.getInputStream()).willReturn(inputStream);
        given(amazonS3.getUrl(eq("test-bucket"), anyString()))
                .willReturn(new URL(expectedUrl));

        // when
        String result = s3Uploader.upload(multipartFile, dirName);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(amazonS3).putObject(eq("test-bucket"), anyString(), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("[실패] 파일 업로드 - 빈 파일명")
    void upload_EmptyFilename_ThrowRuntimeException() throws Exception {
        setUp();
        // given
        String dirName = "test";
        given(multipartFile.getOriginalFilename()).willReturn("");

        // when & then
        assertThatThrownBy(() -> s3Uploader.upload(multipartFile, dirName))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("[실패] 파일 업로드 - null 파일명")
    void upload_NullFilename_ThrowRuntimeException() throws Exception {
        setUp();
        // given
        String dirName = "test";
        given(multipartFile.getOriginalFilename()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> s3Uploader.upload(multipartFile, dirName))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("[성공] 다양한 파일 확장자 업로드")
    void upload_VariousExtensions_Success() throws Exception {
        setUp();
        // given
        String[] filenames = {"test.jpg", "document.pdf", "data.csv", "script.js"};
        String[] contentTypes = {"image/jpeg", "application/pdf", "text/csv", "application/javascript"};
        
        for (int i = 0; i < filenames.length; i++) {
            String dirName = "files";
            String originalFilename = filenames[i];
            String contentType = contentTypes[i];
            byte[] fileContent = "content".getBytes();
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            String expectedUrl = "https://test-bucket.s3.amazonaws.com/files/uuid_" + originalFilename;

            given(multipartFile.getOriginalFilename()).willReturn(originalFilename);
            given(multipartFile.getContentType()).willReturn(contentType);
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getInputStream()).willReturn(inputStream);
            given(amazonS3.getUrl(eq("test-bucket"), anyString()))
                    .willReturn(new URL(expectedUrl));

            // when
            String result = s3Uploader.upload(multipartFile, dirName);

            // then
            assertThat(result).isEqualTo(expectedUrl);
        }
    }
}
