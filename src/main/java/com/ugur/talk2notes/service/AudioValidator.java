package com.ugur.talk2notes.service;

import com.ugur.talk2notes.exception.InvalidRequestException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AudioValidator {
  private static final List<String> SUPPORTED_CONTENT_TYPES =
      Arrays.asList("audio/mpeg", "audio/wav", "audio/mp4", "audio/x-m4a", "audio/ogg");

  private static final List<String> SUPPORTED_EXTENSIONS =
      Arrays.asList("mp3", "wav", "m4a", "ogg");

  private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

  public void validateAudioFile(final MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new InvalidRequestException("Audio file cannot be empty");
    }

    // Validate file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new InvalidRequestException("Audio file size must be less than 100MB");
    }

    // Validate content type
    final String contentType = file.getContentType();
    if (contentType != null && !SUPPORTED_CONTENT_TYPES.contains(contentType)) {
      throw new InvalidRequestException("Unsupported audio format");
    }

    // Validate file extension
    final String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new InvalidRequestException("Unsupported audio format");
    }

    final String extension = FilenameUtils.getExtension(originalFilename);
    if (extension == null
        || extension.isEmpty()
        || !SUPPORTED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new InvalidRequestException("Unsupported audio format");
    }
  }
}
