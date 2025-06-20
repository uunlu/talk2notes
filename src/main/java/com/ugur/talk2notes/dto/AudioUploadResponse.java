package com.ugur.talk2notes.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AudioUploadResponse {
  private Long id;
  private String title;
  private Long fileSize;
  private String status;
  private String message;
  private LocalDateTime uploadedAt;

  public AudioUploadResponse() {}

  public AudioUploadResponse(
      final Long id, final String title, final String status, final String message) {
    this.id = id;
    this.title = title;
    this.status = status;
    this.message = message;
  }

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

  public String getStatus() {
    return this.status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }
}
