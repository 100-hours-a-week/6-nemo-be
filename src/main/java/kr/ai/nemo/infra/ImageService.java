package kr.ai.nemo.infra;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import java.net.URI;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  private static final String MIME_TYPE_JPEG = "image/jpeg";
  private static final String MIME_TYPE_PNG = "image/png";
  private static final String MIME_TYPE_GIF = "image/gif";
  private static final String MIME_TYPE_WEBP = "image/webp";
  private static final String MIME_TYPE_SVG = "image/svg+xml";
  private static final String MIME_TYPE_BMP = "image/bmp";

  private static final String EXT_JPEG = ".jpg";
  private static final String EXT_PNG = ".png";
  private static final String EXT_GIF = ".gif";
  private static final String EXT_WEBP = ".webp";
  private static final String EXT_SVG = ".svg";
  private static final String EXT_BMP = ".bmp";

  public String uploadKakaoProfileImage(String imageUrl, Long userId) {
    try (InputStream inputStream = URI.create(imageUrl).toURL().openStream()) {
      ImageTypeInfo imageInfo = getImageTypeInfo(imageUrl);
      String fileName = String.format("users/%d/profile_%s%s", userId, UUID.randomUUID(), imageInfo.extension);

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(imageInfo.contentType);

      amazonS3.putObject(bucket, fileName, inputStream, metadata);

      return amazonS3.getUrl(bucket, fileName).toString();
    } catch (IOException e) {
      throw new CustomException(CommonErrorCode.S3_UPLOAD_FAILED);
    }
  }

  public String uploadGroupImage(String imageUrl) {
    if (imageUrl == null || imageUrl.trim().isEmpty()) {
        return null;
    }

    if (imageUrl.startsWith("data:image/")) {
        return uploadBase64Image(imageUrl);
    }

    try (InputStream inputStream = URI.create(imageUrl).toURL().openStream()) {
      ImageTypeInfo imageInfo = getImageTypeInfo(imageUrl);
      String fileName = String.format("groups/profile_%s%s", UUID.randomUUID(), imageInfo.extension);

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(imageInfo.contentType);

      amazonS3.putObject(bucket, fileName, inputStream, metadata);

      return amazonS3.getUrl(bucket, fileName).toString();
    } catch (IOException e) {
      log.error("Failed to upload group image. URL: {}, Error: {}", imageUrl, e.getMessage(), e);
      throw new CustomException(CommonErrorCode.S3_UPLOAD_FAILED);
    } catch (Exception e) {
      log.error("Unexpected error during group image upload. URL: {}, Error: {}", imageUrl, e.getMessage(), e);
      throw new CustomException(CommonErrorCode.S3_UPLOAD_FAILED);
    }
  }

  private String uploadBase64Image(String base64Data) {
    try {
      log.info("Processing base64 image. Data starts with: {}", base64Data.substring(0, Math.min(50, base64Data.length())));

      String[] parts = base64Data.split(",", 2);
      if (parts.length != 2) {
        log.error("Invalid base64 format. Parts length: {}", parts.length);
        throw new IllegalArgumentException("Invalid base64 data format");
      }
      
      String header = parts[0];
      String base64Content = parts[1].trim();
      
      log.info("Base64 header: {}", header);
      log.info("Base64 content length before cleaning: {}", base64Content.length());

      base64Content = base64Content.replaceAll("[^A-Za-z0-9+/=]", "");

      int padding = base64Content.length() % 4;
      if (padding > 0) {
        base64Content += "=".repeat(4 - padding);
      }
      
      log.info("Base64 content length after cleaning: {}", base64Content.length());

      String mimeType = MIME_TYPE_JPEG;
      if (header.contains(":") && header.contains(";")) {
        String[] headerParts = header.split(":");
        if (headerParts.length > 1) {
          String mimeAndEncoding = headerParts[1];
          mimeType = mimeAndEncoding.split(";")[0];
        }
      }
      
      log.info("Extracted MIME type: {}", mimeType);

      byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Content);
      log.info("Decoded image size: {} bytes", imageBytes.length);
      
      ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

      ImageTypeInfo imageInfo = getImageTypeInfoFromMimeType(mimeType);
      String fileName = String.format("groups/profile_%s%s", UUID.randomUUID(), imageInfo.extension);
      
      log.info("Uploading to S3 with filename: {} and content-type: {}", fileName, imageInfo.contentType);

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(imageInfo.contentType);
      metadata.setContentLength(imageBytes.length);

      amazonS3.putObject(bucket, fileName, inputStream, metadata);

      String s3Url = amazonS3.getUrl(bucket, fileName).toString();
      log.info("Successfully uploaded base64 image to S3: {}", s3Url);
      
      return s3Url;
    } catch (Exception e) {
      log.error("Failed to upload base64 image. Error: {}", e.getMessage(), e);
      throw new CustomException(CommonErrorCode.S3_UPLOAD_FAILED);
    }
  }

  private ImageTypeInfo getImageTypeInfoFromMimeType(String mimeType) {
    return switch (mimeType.toLowerCase()) {
      case MIME_TYPE_PNG -> new ImageTypeInfo(EXT_PNG, MIME_TYPE_PNG);
      case MIME_TYPE_GIF -> new ImageTypeInfo(EXT_GIF, MIME_TYPE_GIF);
      case MIME_TYPE_WEBP -> new ImageTypeInfo(EXT_WEBP, MIME_TYPE_WEBP);
      case MIME_TYPE_SVG -> new ImageTypeInfo(EXT_SVG, MIME_TYPE_SVG);
      case MIME_TYPE_BMP -> new ImageTypeInfo(EXT_BMP, MIME_TYPE_BMP);
      default -> new ImageTypeInfo(EXT_JPEG, MIME_TYPE_JPEG);
    };
  }

  private ImageTypeInfo getImageTypeInfo(String imageUrl) {
    String originalUrl = imageUrl.toLowerCase();
    
    if (originalUrl.contains(EXT_PNG)) {
      return new ImageTypeInfo(EXT_PNG, MIME_TYPE_PNG);
    } else if (originalUrl.contains(EXT_GIF)) {
      return new ImageTypeInfo(EXT_GIF, MIME_TYPE_GIF);
    } else if (originalUrl.contains(EXT_WEBP)) {
      return new ImageTypeInfo(EXT_WEBP, MIME_TYPE_WEBP);
    } else if (originalUrl.contains(EXT_SVG)) {
      return new ImageTypeInfo(EXT_SVG, MIME_TYPE_SVG);
    } else if (originalUrl.contains(EXT_BMP)) {
      return new ImageTypeInfo(EXT_BMP, MIME_TYPE_BMP);
    } else if (originalUrl.contains(".jpeg") || originalUrl.contains(EXT_JPEG)) {
      return new ImageTypeInfo(EXT_JPEG, MIME_TYPE_JPEG);
    } else {
      return new ImageTypeInfo(EXT_JPEG, MIME_TYPE_JPEG);
    }
  }

  private record ImageTypeInfo(String extension, String contentType) {
  }
}
