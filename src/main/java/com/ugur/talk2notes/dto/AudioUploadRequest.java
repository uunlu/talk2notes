package com.ugur.talk2notes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AudioUploadRequest {
  @NotBlank(message = "Title is required")
  @Size(max = 100, message = "Title must be less than 100 characters")
  private String title;

  @Size(max = 1000, message = "Description must be less than 1000 characters")
  private String description;

  @NotBlank(message = "Language is required")
  @Size(max = 10, message = "Language code must be less than 10 characters")
  private String language;

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

  public String getLanguage() {
    return this.language;
  }

  public void setLanguage(final String language) {
    this.language = language;
  }
}
