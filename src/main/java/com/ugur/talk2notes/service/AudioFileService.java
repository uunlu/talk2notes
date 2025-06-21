package com.ugur.talk2notes.service;

import com.ugur.talk2notes.dto.AudioDetailsDTO;
import com.ugur.talk2notes.exception.ResourceNotFoundException;
import com.ugur.talk2notes.exception.StorageException;
import com.ugur.talk2notes.model.AudioFile;
import com.ugur.talk2notes.model.AudioStatus;
import com.ugur.talk2notes.model.User;
import com.ugur.talk2notes.repository.AudioFileRepository;
import com.ugur.talk2notes.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AudioFileService {
  private final AudioFileRepository audioFileRepository;
  private final LocalAudioStorageService storageService;
  private final UserRepository userRepository;

  public AudioFileService(
      final AudioFileRepository audioFileRepository,
      final LocalAudioStorageService storageService,
      final UserRepository userRepository) {
    this.audioFileRepository = audioFileRepository;
    this.storageService = storageService;
    this.userRepository = userRepository;
  }

  @Transactional
  public AudioFile storeAudio(
      final MultipartFile file,
      final String title,
      final String description,
      final String language,
      final String username) {

    final User user =
        this.userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Create audio file entity
    AudioFile audioFile = new AudioFile();
    audioFile.setTitle(title);
    audioFile.setDescription(description);
    audioFile.setOriginalFilename(file.getOriginalFilename());
    audioFile.setContentType(file.getContentType());
    audioFile.setFileSize(file.getSize());
    audioFile.setStatus(AudioStatus.UPLOADING);
    audioFile.setUploadedAt(LocalDateTime.now());
    audioFile.setLanguage(language);
    audioFile.setUser(user);

    // Save initial entity to get ID
    audioFile = this.audioFileRepository.save(audioFile);

    try {
      // Upload to local storage
      final String storagePath =
          this.storageService.storeFile(file, user.getId(), audioFile.getId());

      // Update with storage path and status
      audioFile.setStorageKey(storagePath);
      audioFile.setStatus(AudioStatus.UPLOADED);
      return this.audioFileRepository.save(audioFile);
    } catch (Exception e) {
      audioFile.setStatus(AudioStatus.FAILED);
      this.audioFileRepository.save(audioFile);
      throw new StorageException("Failed to store audio file", e);
    }
  }

  public AudioFile getAudioFileById(final Long audioId, final String username) {
    final AudioFile audioFile =
        this.audioFileRepository
            .findById(audioId)
            .orElseThrow(() -> new ResourceNotFoundException("Audio file not found"));

    // Security check - ensure user owns this file
    if (!audioFile.getUser().getUsername().equals(username)) {
      throw new SecurityException("You don't have permission to access this file");
    }

    return audioFile;
  }

  public List<AudioDetailsDTO> getUserAudioFiles(final String username) {
    final User user =
        this.userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    final List<AudioFile> audioFiles =
        this.audioFileRepository.findByUserIdOrderByUploadedAtDesc(user.getId());

    return audioFiles.stream().map(this::mapToDetailsDTO).collect(Collectors.toList());
  }

  @Transactional
  public void deleteAudioFile(final Long audioId, final String username) {
    final AudioFile audioFile = this.getAudioFileById(audioId, username);

    // Delete the physical file
    this.storageService.deleteFile(audioFile.getStorageKey());

    // Delete the database record
    this.audioFileRepository.delete(audioFile);
  }

  public AudioDetailsDTO mapToDetailsDTO(final AudioFile audioFile) {
    final var dto = new AudioDetailsDTO();
    dto.setId(audioFile.getId());
    dto.setTitle(audioFile.getTitle());
    dto.setDescription(audioFile.getDescription());
    dto.setOriginalFilename(audioFile.getOriginalFilename());
    dto.setContentType(audioFile.getContentType());
    dto.setFileSize(audioFile.getFileSize());
    dto.setDurationSeconds(audioFile.getDurationSeconds());
    dto.setStatus(audioFile.getStatus().name());
    dto.setUploadedAt(audioFile.getUploadedAt());
    dto.setLanguage(audioFile.getLanguage());
    return dto;
  }
}
