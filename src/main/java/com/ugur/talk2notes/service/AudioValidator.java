package com.ugur.talk2notes.service;

import com.ugur.talk2notes.exception.InvalidRequestException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AudioValidator {
  private static final List<String> SUPPORTED_CONTENT_TYPES =
      Arrays.asList("audio/mpeg", "audio/wav", "audio/mp4", "audio/x-m4a");

  private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("mp3", "wav", "m4a");

  private static final long MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB

  public void validateAudioFile(final MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new InvalidRequestException("Audio file is required");
    }

    // Validate file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new InvalidRequestException("File size exceeds maximum limit of 200MB");
    }

    // Validate content type
    final String contentType = file.getContentType();
    if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType)) {
      throw new InvalidRequestException(
          "Unsupported file format. Supported formats: MP3, WAV, M4A");
    }

    // Validate file extension
    final String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new InvalidRequestException("Invalid filename");
    }

    final String extension =
        originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    if (!SUPPORTED_EXTENSIONS.contains(extension)) {
      throw new InvalidRequestException(
          "Unsupported file extension. Supported extensions: mp3, wav, m4a");
    }
  }
}
