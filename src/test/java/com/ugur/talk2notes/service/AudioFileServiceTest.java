package com.ugur.talk2notes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ugur.talk2notes.dto.AudioDetailsDTO;
import com.ugur.talk2notes.exception.ResourceNotFoundException;
import com.ugur.talk2notes.model.AudioFile;
import com.ugur.talk2notes.model.AudioStatus;
import com.ugur.talk2notes.model.User;
import com.ugur.talk2notes.repository.AudioFileRepository;
import com.ugur.talk2notes.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AudioFileServiceTest {

  @Mock private AudioFileRepository audioFileRepository;

  @Mock private LocalAudioStorageService storageService;

  @Mock private UserRepository userRepository;

  @InjectMocks private AudioFileService audioFileService;

  private User testUser;
  private AudioFile testAudioFile;
  private MultipartFile testFile;

  @BeforeEach
  void setUp() {
    testUser = new User("testuser", "test@example.com", "password");
    testUser.setId(1L);

    testAudioFile = new AudioFile();
    testAudioFile.setId(1L);
    testAudioFile.setTitle("Test Audio");
    testAudioFile.setDescription("Test Description");
    testAudioFile.setOriginalFilename("test.mp3");
    testAudioFile.setContentType("audio/mpeg");
    testAudioFile.setFileSize(1024L);
    testAudioFile.setStorageKey("1/1.mp3");
    testAudioFile.setDurationSeconds(120);
    testAudioFile.setStatus(AudioStatus.UPLOADED);
    testAudioFile.setUploadedAt(LocalDateTime.now());
    testAudioFile.setLanguage("en");
    testAudioFile.setUser(testUser);

    testFile = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "test content".getBytes());
  }

  @Nested
  @DisplayName("storeAudio")
  class StoreAudioTests {

    @Test
    @DisplayName("Should successfully store audio file")
    void shouldStoreAudioFile() {
      // Given
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(storageService.storeFile(any(), anyLong(), anyLong())).thenReturn("1/1.mp3");
      when(audioFileRepository.save(any(AudioFile.class))).thenReturn(testAudioFile);

      // When
      AudioFile result =
          audioFileService.storeAudio(testFile, "Test Audio", "Test Description", "en", "testuser");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getTitle()).isEqualTo("Test Audio");
      assertThat(result.getDescription()).isEqualTo("Test Description");
      assertThat(result.getLanguage()).isEqualTo("en");
      assertThat(result.getUser()).isEqualTo(testUser);
      assertThat(result.getStatus()).isEqualTo(AudioStatus.UPLOADED);

      verify(userRepository).findByUsername("testuser");
      verify(storageService).storeFile(testFile, testUser.getId(), result.getId());
      verify(audioFileRepository, times(2)).save(any(AudioFile.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(
              () ->
                  audioFileService.storeAudio(
                      testFile, "Test Audio", "Test Description", "en", "nonexistent"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("User not found");

      verify(userRepository).findByUsername("nonexistent");
      verify(storageService, times(0)).storeFile(any(), anyLong(), anyLong());
      verify(audioFileRepository, times(0)).save(any());
    }
  }

  @Nested
  @DisplayName("getAudioFileById")
  class GetAudioFileByIdTests {

    @Test
    @DisplayName("Should return audio file when found and user has permission")
    void shouldReturnAudioFileWhenFoundAndUserHasPermission() {
      // Given
      when(audioFileRepository.findById(1L)).thenReturn(Optional.of(testAudioFile));

      // When
      AudioFile result = audioFileService.getAudioFileById(1L, "testuser");

      // Then
      assertThat(result).isEqualTo(testAudioFile);
      verify(audioFileRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when audio file not found")
    void shouldThrowExceptionWhenAudioFileNotFound() {
      // Given
      when(audioFileRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> audioFileService.getAudioFileById(999L, "testuser"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Audio file not found");

      verify(audioFileRepository).findById(999L);
    }

    @Test
    @DisplayName("Should throw security exception when user doesn't have permission")
    void shouldThrowSecurityExceptionWhenUserDoesntHavePermission() {
      // Given
      when(audioFileRepository.findById(1L)).thenReturn(Optional.of(testAudioFile));

      // When & Then
      assertThatThrownBy(() -> audioFileService.getAudioFileById(1L, "otheruser"))
          .isInstanceOf(SecurityException.class)
          .hasMessage("You don't have permission to access this file");

      verify(audioFileRepository).findById(1L);
    }
  }

  @Nested
  @DisplayName("getUserAudioFiles")
  class GetUserAudioFilesTests {

    @Test
    @DisplayName("Should return list of audio files for user")
    void shouldReturnListOfAudioFilesForUser() {
      // Given
      AudioFile audioFile2 = new AudioFile();
      audioFile2.setId(2L);
      audioFile2.setTitle("Test Audio 2");
      audioFile2.setUser(testUser);
      audioFile2.setStatus(AudioStatus.UPLOADED);

      List<AudioFile> audioFiles = Arrays.asList(testAudioFile, audioFile2);
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(audioFileRepository.findByUserIdOrderByUploadedAtDesc(testUser.getId()))
          .thenReturn(audioFiles);

      // When
      List<AudioDetailsDTO> result = audioFileService.getUserAudioFiles("testuser");

      // Then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getId()).isEqualTo(1L);
      assertThat(result.get(1).getId()).isEqualTo(2L);

      verify(userRepository).findByUsername("testuser");
      verify(audioFileRepository).findByUserIdOrderByUploadedAtDesc(testUser.getId());
    }

    @Test
    @DisplayName("Should return empty list when user has no audio files")
    void shouldReturnEmptyListWhenUserHasNoAudioFiles() {
      // Given
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(audioFileRepository.findByUserIdOrderByUploadedAtDesc(testUser.getId()))
          .thenReturn(List.of());

      // When
      List<AudioDetailsDTO> result = audioFileService.getUserAudioFiles("testuser");

      // Then
      assertThat(result).isEmpty();

      verify(userRepository).findByUsername("testuser");
      verify(audioFileRepository).findByUserIdOrderByUploadedAtDesc(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> audioFileService.getUserAudioFiles("nonexistent"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("User not found");

      verify(userRepository).findByUsername("nonexistent");
      verify(audioFileRepository, times(0)).findByUserIdOrderByUploadedAtDesc(anyLong());
    }
  }

  @Nested
  @DisplayName("deleteAudioFile")
  class DeleteAudioFileTests {

    @Test
    @DisplayName("Should successfully delete audio file")
    void shouldDeleteAudioFile() {
      // Given
      when(audioFileRepository.findById(1L)).thenReturn(Optional.of(testAudioFile));
      doNothing().when(storageService).deleteFile(testAudioFile.getStorageKey());
      doNothing().when(audioFileRepository).delete(testAudioFile);

      // When
      audioFileService.deleteAudioFile(1L, "testuser");

      // Then
      verify(audioFileRepository).findById(1L);
      verify(storageService).deleteFile(testAudioFile.getStorageKey());
      verify(audioFileRepository).delete(testAudioFile);
    }

    @Test
    @DisplayName("Should throw exception when audio file not found for deletion")
    void shouldThrowExceptionWhenAudioFileNotFoundForDeletion() {
      // Given
      when(audioFileRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> audioFileService.deleteAudioFile(999L, "testuser"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Audio file not found");

      verify(audioFileRepository).findById(999L);
      verify(storageService, times(0)).deleteFile(anyString());
      verify(audioFileRepository, times(0)).delete(any());
    }
  }

  @Nested
  @DisplayName("mapToDetailsDTO")
  class MapToDetailsDTOTests {

    @Test
    @DisplayName("Should correctly map AudioFile to AudioDetailsDTO")
    void shouldCorrectlyMapAudioFileToAudioDetailsDTO() {
      // When
      AudioDetailsDTO result = audioFileService.mapToDetailsDTO(testAudioFile);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(testAudioFile.getId());
      assertThat(result.getTitle()).isEqualTo(testAudioFile.getTitle());
      assertThat(result.getDescription()).isEqualTo(testAudioFile.getDescription());
      assertThat(result.getOriginalFilename()).isEqualTo(testAudioFile.getOriginalFilename());
      assertThat(result.getContentType()).isEqualTo(testAudioFile.getContentType());
      assertThat(result.getFileSize()).isEqualTo(testAudioFile.getFileSize());
      assertThat(result.getDurationSeconds()).isEqualTo(testAudioFile.getDurationSeconds());
      assertThat(result.getStatus()).isEqualTo(testAudioFile.getStatus().name());
      assertThat(result.getUploadedAt()).isEqualTo(testAudioFile.getUploadedAt());
      assertThat(result.getLanguage()).isEqualTo(testAudioFile.getLanguage());
    }

    @Test
    @DisplayName("Should handle null values in AudioFile")
    void shouldHandleNullValuesInAudioFile() {
      // Given
      AudioFile audioFileWithNulls = new AudioFile();
      audioFileWithNulls.setId(1L);
      audioFileWithNulls.setTitle("Test");
      audioFileWithNulls.setStatus(AudioStatus.UPLOADED);
      audioFileWithNulls.setUploadedAt(LocalDateTime.now());

      // When
      AudioDetailsDTO result = audioFileService.mapToDetailsDTO(audioFileWithNulls);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getTitle()).isEqualTo("Test");
      assertThat(result.getDescription()).isNull();
      assertThat(result.getOriginalFilename()).isNull();
      assertThat(result.getContentType()).isNull();
      assertThat(result.getFileSize()).isNull();
      assertThat(result.getDurationSeconds()).isNull();
      assertThat(result.getLanguage()).isNull();
    }
  }

  // Mock MultipartFile implementation for testing
  private static class MockMultipartFile implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public MockMultipartFile(
        String name, String originalFilename, String contentType, byte[] content) {
      this.name = name;
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.content = content;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getOriginalFilename() {
      return originalFilename;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public boolean isEmpty() {
      return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
      return content != null ? content.length : 0;
    }

    @Override
    public byte[] getBytes() {
      return content;
    }

    @Override
    public java.io.InputStream getInputStream() {
      return new java.io.ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) {
      // Not implemented for tests
    }
  }
}
