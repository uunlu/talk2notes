package com.ugur.talk2notes.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ugur.talk2notes.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AudioValidatorTest {

  @Mock private MultipartFile mockFile;

  private AudioValidator audioValidator;

  private static final String TEST_CONTENT = "test audio content";

  @BeforeEach
  void setUp() {
    audioValidator = new AudioValidator();
  }

  @Nested
  @DisplayName("validateAudioFile")
  class ValidateAudioFileTests {

    @Test
    @DisplayName("Should validate valid MP3 file")
    void shouldValidateValidMp3File() {
      // Given
      byte[] content = TEST_CONTENT.getBytes();
      when(mockFile.getOriginalFilename()).thenReturn("test.mp3");
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.isEmpty()).thenReturn(false);

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).getOriginalFilename();
      verify(mockFile).getContentType();
      verify(mockFile).getSize();
      verify(mockFile).isEmpty();
    }

    @Test
    @DisplayName("Should validate valid WAV file")
    void shouldValidateValidWavFile() {
      // Given
      byte[] content = TEST_CONTENT.getBytes();
      when(mockFile.getOriginalFilename()).thenReturn("test.wav");
      when(mockFile.getContentType()).thenReturn("audio/wav");
      when(mockFile.getSize()).thenReturn(2048L);
      when(mockFile.isEmpty()).thenReturn(false);

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).getOriginalFilename();
      verify(mockFile).getContentType();
      verify(mockFile).getSize();
      verify(mockFile).isEmpty();
    }

    @Test
    @DisplayName("Should validate valid M4A file")
    void shouldValidateValidM4aFile() {
      // Given
      byte[] content = TEST_CONTENT.getBytes();
      when(mockFile.getOriginalFilename()).thenReturn("test.m4a");
      when(mockFile.getContentType()).thenReturn("audio/mp4");
      when(mockFile.getSize()).thenReturn(3072L);
      when(mockFile.isEmpty()).thenReturn(false);

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).getOriginalFilename();
      verify(mockFile).getContentType();
      verify(mockFile).getSize();
      verify(mockFile).isEmpty();
    }

    @Test
    @DisplayName("Should validate valid OGG file")
    void shouldValidateValidOggFile() {
      // Given
      byte[] content = TEST_CONTENT.getBytes();
      when(mockFile.getOriginalFilename()).thenReturn("test.ogg");
      when(mockFile.getContentType()).thenReturn("audio/ogg");
      when(mockFile.getSize()).thenReturn(4096L);
      when(mockFile.isEmpty()).thenReturn(false);

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).getOriginalFilename();
      verify(mockFile).getContentType();
      verify(mockFile).getSize();
      verify(mockFile).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void shouldThrowExceptionWhenFileIsEmpty() {
      // Given
      when(mockFile.isEmpty()).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> audioValidator.validateAudioFile(mockFile))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessageContaining("Audio file cannot be empty");

      verify(mockFile).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when file size exceeds limit")
    void shouldThrowExceptionWhenFileSizeExceedsLimit() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB

      // When & Then
      assertThatThrownBy(() -> audioValidator.validateAudioFile(mockFile))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessageContaining("Audio file size must be less than 100MB");

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
    }

    @Test
    @DisplayName("Should throw exception when content type is not supported")
    void shouldThrowExceptionWhenContentTypeIsNotSupported() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn("image/jpeg");

      // When & Then
      assertThatThrownBy(() -> audioValidator.validateAudioFile(mockFile))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessageContaining("Unsupported audio format");

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
    }

    @Test
    @DisplayName("Should throw exception when file extension is not supported")
    void shouldThrowExceptionWhenFileExtensionIsNotSupported() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getOriginalFilename()).thenReturn("test.txt");

      // When & Then
      assertThatThrownBy(() -> audioValidator.validateAudioFile(mockFile))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessageContaining("Unsupported audio format");

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should handle null content type")
    void shouldHandleNullContentType() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn(null);
      when(mockFile.getOriginalFilename()).thenReturn("test.mp3");

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should handle null original filename")
    void shouldHandleNullOriginalFilename() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getOriginalFilename()).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> audioValidator.validateAudioFile(mockFile))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessageContaining("Unsupported audio format");

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should validate file with maximum allowed size")
    void shouldValidateFileWithMaximumAllowedSize() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(100L * 1024 * 1024); // 100MB
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getOriginalFilename()).thenReturn("test.mp3");

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should validate file with zero size")
    void shouldValidateFileWithZeroSize() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(0L);
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getOriginalFilename()).thenReturn("test.mp3");

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should handle uppercase file extensions")
    void shouldHandleUppercaseFileExtensions() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getOriginalFilename()).thenReturn("test.MP3");

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should handle mixed case file extensions")
    void shouldHandleMixedCaseFileExtensions() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn("audio/wav");
      when(mockFile.getOriginalFilename()).thenReturn("test.WaV");

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should handle files with multiple dots in filename")
    void shouldHandleFilesWithMultipleDotsInFilename() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getOriginalFilename()).thenReturn("test.audio.mp3");

      // When & Then
      assertThatCode(() -> audioValidator.validateAudioFile(mockFile)).doesNotThrowAnyException();

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }

    @Test
    @DisplayName("Should handle files with no extension")
    void shouldHandleFilesWithNoExtension() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.getContentType()).thenReturn("audio/mpeg");
      when(mockFile.getOriginalFilename()).thenReturn("testfile");

      // When & Then
      assertThatThrownBy(() -> audioValidator.validateAudioFile(mockFile))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessageContaining("Unsupported audio format");

      verify(mockFile).isEmpty();
      verify(mockFile).getSize();
      verify(mockFile).getContentType();
      verify(mockFile).getOriginalFilename();
    }
  }
}
