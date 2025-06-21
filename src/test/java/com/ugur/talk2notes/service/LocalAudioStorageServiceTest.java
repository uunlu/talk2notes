package com.ugur.talk2notes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ugur.talk2notes.exception.StorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class LocalAudioStorageServiceTest {

  @TempDir Path tempDir;
  private LocalAudioStorageService storageService;
  private Path testUploadsDir;

  @BeforeEach
  void setUp() throws IOException {
    // Create a temporary uploads directory for testing
    testUploadsDir = tempDir.resolve("uploads");
    Files.createDirectories(testUploadsDir);

    // Use reflection to set the rootLocation to our temp directory
    storageService = new LocalAudioStorageService();
    try {
      var rootLocationField = LocalAudioStorageService.class.getDeclaredField("rootLocation");
      rootLocationField.setAccessible(true);
      rootLocationField.set(storageService, testUploadsDir);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set test root location", e);
    }
  }

  @Nested
  class StoreFileTests {

    @Test
    void shouldSuccessfullyStoreFile() throws IOException {
      // Given
      String content = "test audio content";
      MockMultipartFile file =
          new MockMultipartFile("file", "test-audio.mp3", "audio/mpeg", content.getBytes());

      // When
      String result = storageService.storeFile(file, 1L, 1L);

      // Then
      assertEquals("1/1/test-audio.mp3", result);

      Path expectedFile = testUploadsDir.resolve("1/1/test-audio.mp3");
      assertTrue(Files.exists(expectedFile));
      assertEquals(content, new String(Files.readAllBytes(expectedFile)));
    }

    @Test
    void shouldHandleFileWithNoExtension() throws IOException {
      // Given
      String content = "test audio content";
      MockMultipartFile file =
          new MockMultipartFile("file", "test-audio", "audio/mpeg", content.getBytes());

      // When
      String result = storageService.storeFile(file, 1L, 1L);

      // Then
      assertEquals("1/1/test-audio", result);

      Path expectedFile = testUploadsDir.resolve("1/1/test-audio");
      assertTrue(Files.exists(expectedFile));
      assertEquals(content, new String(Files.readAllBytes(expectedFile)));
    }

    @Test
    void shouldHandleNullOriginalFilename() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", null, "audio/mpeg", "test content".getBytes());

      // When & Then
      StorageException exception =
          assertThrows(StorageException.class, () -> storageService.storeFile(file, 1L, 1L));
      assertEquals("Original filename cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldHandleEmptyOriginalFilename() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "", "audio/mpeg", "test content".getBytes());

      // When & Then
      StorageException exception =
          assertThrows(StorageException.class, () -> storageService.storeFile(file, 1L, 1L));
      assertEquals("Original filename cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowStorageExceptionWhenFileIsEmpty() {
      // Given
      MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[0]);

      // When & Then
      StorageException exception =
          assertThrows(StorageException.class, () -> storageService.storeFile(file, 1L, 1L));
      assertEquals("Failed to store empty file", exception.getMessage());
    }

    @Test
    void shouldThrowStorageExceptionWhenIOExceptionOccurs() throws IOException {
      // Given
      MultipartFile mockFile = mock(MultipartFile.class);
      when(mockFile.isEmpty()).thenReturn(false);
      when(mockFile.getOriginalFilename()).thenReturn("test.mp3");
      when(mockFile.getInputStream()).thenThrow(new IOException("Test IO exception"));

      // When & Then
      StorageException exception =
          assertThrows(StorageException.class, () -> storageService.storeFile(mockFile, 1L, 1L));
      assertEquals("Failed to store file", exception.getMessage());
      assertTrue(exception.getCause() instanceof IOException);
    }
  }

  @Nested
  class LoadFileAsResourceTests {

    @Test
    void shouldLoadExistingFileAsResource() throws IOException {
      // Given
      String content = "test content";
      Path testFile = testUploadsDir.resolve("test.txt");
      Files.write(testFile, content.getBytes());

      // When
      Resource resource = storageService.loadFileAsResource("test.txt");

      // Then
      assertNotNull(resource);
      assertTrue(resource.exists());
      assertTrue(resource.isReadable());
      assertEquals(content, new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    void shouldThrowStorageExceptionForNonExistentFile() {
      // When & Then
      StorageException exception =
          assertThrows(
              StorageException.class, () -> storageService.loadFileAsResource("nonexistent.txt"));
      assertEquals("Could not read file: nonexistent.txt", exception.getMessage());
    }
  }

  @Nested
  class DeleteFileTests {

    @Test
    void shouldDeleteExistingFile() throws IOException {
      // Given
      Path testFile = testUploadsDir.resolve("test.txt");
      Files.write(testFile, "test content".getBytes());
      assertTrue(Files.exists(testFile));

      // When
      storageService.deleteFile("test.txt");

      // Then
      assertFalse(Files.exists(testFile));
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentFile() {
      // When & Then - should not throw exception
      assertDoesNotThrow(() -> storageService.deleteFile("nonexistent.txt"));
    }
  }

  @Nested
  class EdgeCasesTests {

    @Test
    void shouldHandleVeryLongFilenames() throws IOException {
      // Given
      String longName = "a".repeat(200) + ".mp3";
      String content = "test content";
      MockMultipartFile file =
          new MockMultipartFile("file", longName, "audio/mpeg", content.getBytes());

      // When
      String result = storageService.storeFile(file, 1L, 1L);

      // Then
      assertEquals("1/1/" + longName, result);
      Path expectedFile = testUploadsDir.resolve("1/1/" + longName);
      assertTrue(Files.exists(expectedFile));
    }

    @Test
    void shouldHandleSpecialCharactersInFilename() throws IOException {
      // Given
      String specialName = "test@#$%^&*()_+-=[]{}|;':\",./<>?`~.mp3";
      String content = "test content";
      MockMultipartFile file =
          new MockMultipartFile("file", specialName, "audio/mpeg", content.getBytes());

      // When
      String result = storageService.storeFile(file, 1L, 1L);

      // Then
      assertEquals("1/1/" + specialName, result);
      Path expectedFile = testUploadsDir.resolve("1/1/" + specialName);
      assertTrue(Files.exists(expectedFile));
    }

    @Test
    void shouldHandleZeroByteFile() throws IOException {
      // Given
      MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[0]);

      // When & Then
      StorageException exception =
          assertThrows(StorageException.class, () -> storageService.storeFile(file, 1L, 1L));
      assertEquals("Failed to store empty file", exception.getMessage());
    }
  }
}
