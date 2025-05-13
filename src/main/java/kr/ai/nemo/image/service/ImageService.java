package kr.ai.nemo.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.net.URI;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public String uploadKakaoProfileImage(String imageUrl, Long userId) {
    try (InputStream inputStream = URI.create(imageUrl).toURL().openStream()) {
      String fileName = String.format("users/%d/profile_%s.jpg", userId, UUID.randomUUID());

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType("image/jpeg");

      amazonS3.putObject(bucket, fileName, inputStream, metadata);

      return amazonS3.getUrl(bucket, fileName).toString();
    } catch (IOException e) {
      throw new CustomException(ResponseCode.S3_UPLOAD_FAILED);
    }
  }

  public String uploadGroupImage(String imageUrl) {
    try (InputStream inputStream = URI.create(imageUrl).toURL().openStream()) {
      String fileName = String.format("groups/profile_%s.jpg", UUID.randomUUID());

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType("image/jpeg");

      amazonS3.putObject(bucket, fileName, inputStream, metadata);

      return amazonS3.getUrl(bucket, fileName).toString();
    } catch (IOException e) {
      throw new CustomException(ResponseCode.S3_UPLOAD_FAILED);
    }
  }
}
