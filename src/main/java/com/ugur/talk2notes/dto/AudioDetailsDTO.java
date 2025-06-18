package com.ugur.talk2notes.dto;

import java.time.LocalDateTime;

public class AudioDetailsDTO {
  private Long id;
  private String title;
  private String description;
  private String originalFilename;
  private String contentType;
  private Long fileSize;
  private Integer durationSeconds;
  private String status;
  private LocalDateTime uploadedAt;
  private String language;

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getOriginalFilename() {
    return this.originalFilename;
  }

  public void setOriginalFilename(final String originalFilename) {
    this.originalFilename = originalFilename;
  }

  public String getContentType() {
    return this.contentType;
  }

  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  public Long getFileSize() {
    return this.fileSize;
  }

  public void setFileSize(final Long fileSize) {
    this.fileSize = fileSize;
  }

  public Integer getDurationSeconds() {
    return this.durationSeconds;
  }

  public void setDurationSeconds(final Integer durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public LocalDateTime getUploadedAt() {
    return this.uploadedAt;
  }

  public void setUploadedAt(final LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public String getLanguage() {
    return this.language;
  }

  public void setLanguage(final String language) {
    this.language = language;
  }
}
