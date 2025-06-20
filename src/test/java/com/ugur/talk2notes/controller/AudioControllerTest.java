package com.ugur.talk2notes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ugur.talk2notes.dto.AudioDetailsDTO;
import com.ugur.talk2notes.dto.AudioUploadRequest;
import com.ugur.talk2notes.dto.AudioUploadResponse;
import com.ugur.talk2notes.exception.InvalidRequestException;
import com.ugur.talk2notes.exception.ResourceNotFoundException;
import com.ugur.talk2notes.model.AudioFile;
import com.ugur.talk2notes.model.AudioStatus;
import com.ugur.talk2notes.model.User;
import com.ugur.talk2notes.service.AudioFileService;
import com.ugur.talk2notes.validation.AudioValidator;
import com.ugur.talk2notes.service.LocalAudioStorageService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AudioControllerTest {

  @Mock private AudioFileService audioFileService;

  @Mock private AudioValidator audioValidator;

  @Mock private LocalAudioStorageService storageService;

  @Mock private MultipartFile mockFile;

  @InjectMocks private AudioController audioController;

  private User testUser;
  private AudioFile testAudioFile;
  private AudioDetailsDTO testAudioDetailsDTO;

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

    testAudioDetailsDTO = new AudioDetailsDTO();
    testAudioDetailsDTO.setId(1L);
    testAudioDetailsDTO.setTitle("Test Audio");
    testAudioDetailsDTO.setDescription("Test Description");
    testAudioDetailsDTO.setOriginalFilename("test.mp3");
    testAudioDetailsDTO.setContentType("audio/mpeg");
    testAudioDetailsDTO.setFileSize(1024L);
    testAudioDetailsDTO.setDurationSeconds(120);
    testAudioDetailsDTO.setStatus(AudioStatus.UPLOADED.name());
    testAudioDetailsDTO.setUploadedAt(LocalDateTime.now());
    testAudioDetailsDTO.setLanguage("en");
  }

  @Nested
  @DisplayName("uploadAudio")
  class UploadAudioTests {

    @Test
    @DisplayName("Should successfully upload audio")
    void shouldSuccessfullyUploadAudio() {
      // Given
      AudioUploadRequest request = new AudioUploadRequest();
      request.setTitle("Test Audio");
      request.setDescription("Test Description");
      request.setLanguage("en");

      when(audioFileService.storeAudio(any(), anyString(), anyString(), anyString(), anyString()))
          .thenReturn(testAudioFile);

      // When
      ResponseEntity<AudioUploadResponse> response = audioController.uploadAudio(mockFile, request);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getId()).isEqualTo(1L);
      assertThat(response.getBody().getMessage()).isEqualTo("Audio uploaded successfully");

      verify(audioValidator).validateAudioFile(mockFile);
      verify(audioFileService)
          .storeAudio(mockFile, "Test Audio", "Test Description", "en", "default");
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void shouldThrowExceptionWhenValidationFails() {
      // Given
      AudioUploadRequest request = new AudioUploadRequest();
      request.setTitle("Test Audio");
      request.setDescription("Test Description");
      request.setLanguage("en");

      org.mockito.Mockito.doThrow(new InvalidRequestException("Invalid file"))
          .when(audioValidator)
          .validateAudioFile(mockFile);

      // When & Then
      assertThatThrownBy(() -> audioController.uploadAudio(mockFile, request))
          .isInstanceOf(InvalidRequestException.class)
          .hasMessage("Invalid file");

      verify(audioValidator).validateAudioFile(mockFile);
      verify(audioFileService, org.mockito.Mockito.never())
          .storeAudio(any(), anyString(), anyString(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("getAudioDetails")
  class GetAudioDetailsTests {

    @Test
    @DisplayName("Should return audio details when found")
    void shouldReturnAudioDetailsWhenFound() {
      // Given
      when(audioFileService.getAudioFileById(1L, "default")).thenReturn(testAudioFile);
      when(audioFileService.mapToDetailsDTO(testAudioFile)).thenReturn(testAudioDetailsDTO);
      // When
      ResponseEntity<AudioDetailsDTO> response = audioController.getAudioDetails(1L);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getId()).isEqualTo(1L);
      assertThat(response.getBody().getTitle()).isEqualTo("Test Audio");

      verify(audioFileService).getAudioFileById(1L, "default");
    }

    @Test
    @DisplayName("Should throw exception when audio not found")
    void shouldThrowExceptionWhenAudioNotFound() {
      // Given
      when(audioFileService.getAudioFileById(999L, "default"))
          .thenThrow(new ResourceNotFoundException("Audio not found"));

      // When & Then
      assertThatThrownBy(() -> audioController.getAudioDetails(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Audio not found");

      verify(audioFileService).getAudioFileById(999L, "default");
    }
  }

  @Nested
  @DisplayName("getAudioContent")
  class GetAudioContentTests {

    @Test
    @DisplayName("Should return audio content when found")
    void shouldReturnAudioContentWhenFound() {
      // Given
      when(audioFileService.getAudioFileById(1L, "default")).thenReturn(testAudioFile);
      Resource mockResource = new ByteArrayResource("test content".getBytes());
      when(storageService.loadFileAsResource("1/1.mp3")).thenReturn(mockResource);

      // When
      ResponseEntity<Resource> response = audioController.getAudioContent(1L);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getHeaders().getContentType()).isNotNull();

      verify(audioFileService).getAudioFileById(1L, "default");
      verify(storageService).loadFileAsResource("1/1.mp3");
    }

    @Test
    @DisplayName("Should throw exception when audio not found")
    void shouldThrowExceptionWhenAudioNotFound() {
      // Given
      when(audioFileService.getAudioFileById(999L, "default"))
          .thenThrow(new ResourceNotFoundException("Audio not found"));

      // When & Then
      assertThatThrownBy(() -> audioController.getAudioContent(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Audio not found");

      verify(audioFileService).getAudioFileById(999L, "default");
      verify(storageService, org.mockito.Mockito.never()).loadFileAsResource(anyString());
    }
  }

  @Nested
  @DisplayName("listUserAudioFiles")
  class ListUserAudioFilesTests {

    @Test
    @DisplayName("Should return list of user audio files")
    void shouldReturnListOfUserAudioFiles() {
      // Given
      AudioDetailsDTO audioDetails2 = new AudioDetailsDTO();
      audioDetails2.setId(2L);
      audioDetails2.setTitle("Test Audio 2");

      List<AudioDetailsDTO> audioFiles = Arrays.asList(testAudioDetailsDTO, audioDetails2);
      when(audioFileService.getUserAudioFiles("default")).thenReturn(audioFiles);

      // When
      ResponseEntity<List<AudioDetailsDTO>> response = audioController.listUserAudioFiles();

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody()).hasSize(2);
      assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
      assertThat(response.getBody().get(1).getId()).isEqualTo(2L);

      verify(audioFileService).getUserAudioFiles("default");
    }

    @Test
    @DisplayName("Should return empty list when user has no audio files")
    void shouldReturnEmptyListWhenUserHasNoAudioFiles() {
      // Given
      when(audioFileService.getUserAudioFiles("default")).thenReturn(List.of());

      // When
      ResponseEntity<List<AudioDetailsDTO>> response = audioController.listUserAudioFiles();

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody()).isEmpty();

      verify(audioFileService).getUserAudioFiles("default");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(audioFileService.getUserAudioFiles("default"))
          .thenThrow(new ResourceNotFoundException("User not found"));

      // When & Then
      assertThatThrownBy(() -> audioController.listUserAudioFiles())
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("User not found");

      verify(audioFileService).getUserAudioFiles("default");
    }
  }

  @Nested
  @DisplayName("deleteAudioFile")
  class DeleteAudioFileTests {

    @Test
    @DisplayName("Should successfully delete audio file")
    void shouldSuccessfullyDeleteAudioFile() {
      // Given
      when(audioFileService.getAudioFileById(1L, "default")).thenReturn(testAudioFile);

      // When
      ResponseEntity<Void> response = audioController.deleteAudioFile(1L);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      verify(audioFileService).getAudioFileById(1L, "default");
      verify(audioFileService).deleteAudioFile(1L, "default");
    }

    @Test
    @DisplayName("Should throw exception when audio not found")
    void shouldThrowExceptionWhenAudioNotFound() {
      // Given
      when(audioFileService.getAudioFileById(999L, "default"))
          .thenThrow(new ResourceNotFoundException("Audio not found"));

      // When & Then
      assertThatThrownBy(() -> audioController.deleteAudioFile(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Audio not found");

      verify(audioFileService).getAudioFileById(999L, "default");
      verify(audioFileService, org.mockito.Mockito.never()).deleteAudioFile(anyLong(), anyString());
    }
  }
}
