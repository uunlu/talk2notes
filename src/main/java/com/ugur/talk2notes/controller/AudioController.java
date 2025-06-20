package com.ugur.talk2notes.controller;

import com.ugur.talk2notes.dto.AudioDetailsDTO;
import com.ugur.talk2notes.dto.AudioUploadRequest;
import com.ugur.talk2notes.dto.AudioUploadResponse;
import com.ugur.talk2notes.model.AudioFile;
import com.ugur.talk2notes.service.AudioFileService;
import com.ugur.talk2notes.service.LocalAudioStorageService;
import com.ugur.talk2notes.validation.AudioValidator;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {
  private final AudioFileService audioFileService;
  private final AudioValidator audioValidator;
  private final LocalAudioStorageService storageService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AudioUploadResponse> uploadAudio(
      @RequestParam("file") final MultipartFile file,
      @Valid @ModelAttribute final AudioUploadRequest request) {

    this.audioValidator.validateAudioFile(file);

    final AudioFile audioFile =
        this.audioFileService.storeAudio(
            file,
            request.getTitle(),
            request.getDescription(),
            request.getLanguage(),
            "default" // TODO: Replace with actual user ID when authentication is implemented
            );

    final var response = new AudioUploadResponse();
    response.setId(audioFile.getId());
    response.setTitle(audioFile.getTitle());
    response.setStatus(audioFile.getStatus().name());
    response.setMessage("Audio uploaded successfully");

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{audioId}")
  public ResponseEntity<AudioDetailsDTO> getAudioDetails(@PathVariable final Long audioId) {
    final AudioFile audioFile =
        this.audioFileService.getAudioFileById(
            audioId, "default"); // TODO: Replace with actual user ID
    return ResponseEntity.ok(this.audioFileService.mapToDetailsDTO(audioFile));
  }

  @GetMapping("/{audioId}/content")
  public ResponseEntity<Resource> getAudioContent(@PathVariable final Long audioId) {
    final AudioFile audioFile =
        this.audioFileService.getAudioFileById(
            audioId, "default"); // TODO: Replace with actual user ID
    final Resource resource = this.storageService.loadFileAsResource(audioFile.getStorageKey());

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(audioFile.getContentType()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + audioFile.getOriginalFilename() + "\"")
        .body(resource);
  }

  @GetMapping
  public ResponseEntity<List<AudioDetailsDTO>> listUserAudioFiles() {
    final List<AudioDetailsDTO> audioFiles =
        this.audioFileService.getUserAudioFiles("default"); // TODO: Replace with actual user ID
    return ResponseEntity.ok(audioFiles);
  }

  @DeleteMapping("/{audioId}")
  public ResponseEntity<Void> deleteAudioFile(@PathVariable final Long audioId) {
    // First check if the file exists (this will throw ResourceNotFoundException if not found)
    this.audioFileService.getAudioFileById(audioId, "default");
    // TODO: Replace with actual user ID
    // Then delete it
    this.audioFileService.deleteAudioFile(audioId, "default");
    return ResponseEntity.noContent().build();
  }
}
