package com.ugur.talk2notes.service;

import com.ugur.talk2notes.exception.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalAudioStorageService {
  private final Path rootLocation;

  public LocalAudioStorageService() {
    this.rootLocation = Paths.get("uploads");
    try {
      Files.createDirectories(this.rootLocation);
    } catch (IOException e) {
      throw new StorageException("Could not initialize storage location", e);
    }
  }

  public String storeFile(final MultipartFile file, final Long userId, final Long audioId) {
    try {
      if (file.isEmpty()) {
        throw new StorageException("Failed to store empty file");
      }

      // Get original filename and sanitize it, but preserve special chars
      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null || originalFilename.trim().isEmpty()) {
        throw new StorageException("Original filename cannot be null or empty");
      }

      // Clean the path, but don't use FilenameUtils which might misinterpret chars
      originalFilename = StringUtils.cleanPath(originalFilename);

      // Handle file extension separately
      String extension = "";
      final int lastDotIndex = originalFilename.lastIndexOf('.');
      if (lastDotIndex > 0) {
        extension = originalFilename.substring(lastDotIndex);
        originalFilename = originalFilename.substring(0, lastDotIndex);
      }

      // Construct the filename with sanitized base + extension
      final String filename = originalFilename + extension;

      // Rest of your method remains the same...
      final Path targetLocation =
              this.rootLocation.resolve(String.format("%d/%d", userId, audioId)).resolve(filename);

      Files.createDirectories(targetLocation.getParent());

      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
      }

      return String.format("%d/%d/%s", userId, audioId, filename);
    } catch (IOException e) {
      throw new StorageException("Failed to store file", e);
    }
  }

  public Resource loadFileAsResource(final String filename) {
    try {
      final Path file = this.rootLocation.resolve(filename);
      final Resource resource = new UrlResource(file.toUri());

      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new StorageException("Could not read file: " + filename);
      }
    } catch (MalformedURLException e) {
      throw new StorageException("Could not read file: " + filename, e);
    }
  }

  public void deleteFile(final String filename) {
    try {
      final Path file = this.rootLocation.resolve(filename);
      Files.deleteIfExists(file);
    } catch (IOException e) {
      throw new StorageException("Could not delete file: " + filename, e);
    }
  }
}
